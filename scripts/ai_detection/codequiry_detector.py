"""Package eligible additions and obtain complete, revision-bound Codequiry scores."""

from decimal import Decimal
import hashlib
from io import BytesIO
import json
import math
from pathlib import Path
import time
from zipfile import ZipFile, ZipInfo

from .codequiry import CodequiryClient, CodequiryError, _positive_integer
from .diff import Component


_EXTENSIONS = {"java": ".java", "kotlin": ".kt", "python": ".py",
               "typescript": ".ts", "javascript": ".js"}


def package(components: tuple[Component, ...]) -> tuple[bytes, dict[str, str]]:
    """Deterministic opaque ZIP names; original paths stay on the local machine."""
    if not components or len({c.component_id for c in components}) != len(components):
        raise CodequiryError("Expected nonempty components with unique IDs.")
    buffer = BytesIO()
    mapping = {}
    size = 0
    with ZipFile(buffer, "w") as archive:
        for index, component in enumerate(components, 1):
            if component.language not in _EXTENSIONS:
                raise CodequiryError("Unsupported component language.")
            path = f"submission/component_{index:05d}{_EXTENSIONS[component.language]}"
            content = "\n".join(component.block.lines).encode("utf-8")
            size += len(content)
            if size >= 9_000_000:
                raise CodequiryError("Eligible code exceeds the submission size budget.")
            archive.writestr(ZipInfo(path), content)
            mapping[path] = component.component_id
    if len(buffer.getvalue()) >= 10_000_000:
        raise CodequiryError("Submission archive exceeds the provider size limit.")
    return buffer.getvalue(), mapping


def component_scores(payload: object, check_id: int, mapping: dict[str, str]) -> dict[str, Decimal]:
    if not isinstance(payload, dict) or payload.get("success") is not True:
        raise CodequiryError("Missing successful AI result.")
    if payload.get("assignment_id") != check_id or payload.get("assignment_status") != 4:
        raise CodequiryError("AI result does not match the completed check.")
    submissions = payload.get("submissions")
    if not isinstance(submissions, list) or len(submissions) != 1:
        raise CodequiryError("Expected exactly one completed submission.")
    submission = submissions[0]
    if not isinstance(submission, dict) or submission.get("status_id") != 4:
        raise CodequiryError("Submission analysis is incomplete.")
    detection = submission.get("ai_detection")
    files = detection.get("files") if isinstance(detection, dict) else None
    if not isinstance(files, list) or detection.get("total_files") != len(mapping):
        raise CodequiryError("AI result coverage differs from the submission.")
    scores = {}
    for entry in files:
        if not isinstance(entry, dict):
            raise CodequiryError("Malformed AI file result.")
        path = entry.get("file_path")
        # Codequiry may report a path relative to the submission's root folder.
        if isinstance(path, str) and not path.startswith("submission/"):
            path = "submission/" + path
        component_id = mapping.get(path) if isinstance(path, str) else None
        if component_id is None or component_id in scores:
            raise CodequiryError("Unexpected or duplicate AI file result.")
        raw = entry.get("ai_probability")
        if type(raw) not in (int, Decimal):
            raise CodequiryError("AI probability must be a JSON number.")
        value = Decimal(raw)
        if not value.is_finite() or not 0 <= value <= 100:
            raise CodequiryError("AI probability is outside the 0–100 scale.")
        scores[component_id] = value
    if set(scores) != set(mapping.values()):
        raise CodequiryError("AI results omit submitted components.")
    return scores


class CodequiryDetector:
    """Use a private receipt to resume polling without replaying paid mutations."""

    def __init__(self, client: CodequiryClient, *, test_type: int, receipt: Path,
                 revision: str, timeout: float = 480, delete_after: bool = False):
        _positive_integer(test_type)
        if not math.isfinite(timeout) or not 1 <= timeout <= 900:
            raise ValueError("Analysis timeout must be between 1 and 900 seconds")
        self.client = client
        self.test_type = test_type
        self.receipt = receipt
        self.revision = revision
        self.timeout = timeout
        self.delete_after = delete_after
        self.check_id = None
        self.cleanup = "not_requested"

    def _save(self, receipt: dict) -> None:
        # The lock serializes readers and writers of this receipt. A partial write
        # is rejected on the next run, so it cannot authorize a repeated mutation.
        with self.receipt.open("w", encoding="utf-8") as out:
            json.dump(receipt, out)

    def _read(self, operation, deadline):
        for attempt in range(3):
            if time.monotonic() >= deadline:
                raise CodequiryError("Codequiry analysis deadline exceeded.")
            try:
                return operation()
            except CodequiryError as error:
                if error.status not in (None, 429, 500, 502, 503, 504) or attempt == 2:
                    raise
                if time.monotonic() + error.retry_after >= deadline:
                    raise CodequiryError("Codequiry retry exceeds the analysis deadline.") from None
                time.sleep(error.retry_after)
        raise AssertionError("Unreachable retry state")

    def score(self, components: tuple[Component, ...]) -> dict[str, Decimal]:
        archive, mapping = package(components)
        settings = json.dumps({"revision": self.revision, "test_type": self.test_type,
                               "adapter_version": 1}, sort_keys=True).encode()
        digest = hashlib.sha256(archive + settings).hexdigest()
        lock_path = self.receipt.with_name(self.receipt.name + ".lock")
        try:
            lock = lock_path.open("x")
        except FileExistsError:
            raise CodequiryError("Receipt is locked; reconcile the prior run before retrying.") from None
        try:
            with lock:
                return self._run(archive, mapping, digest)
        finally:
            lock_path.unlink()

    def _run(self, archive, mapping, digest):
        deadline = time.monotonic() + self.timeout
        if self.receipt.exists():
            try:
                saved = json.loads(self.receipt.read_text(encoding="utf-8"))
            except (ValueError, OSError):
                raise CodequiryError("Unreadable receipt; reconcile the provider job.") from None
            if not isinstance(saved, dict) or saved.get("digest") != digest:
                raise CodequiryError("Receipt belongs to different inputs or settings.")
            if saved.get("stage") not in ("start_requested", "completed"):
                raise CodequiryError("An earlier mutation is ambiguous; reconcile it before retrying.")
            self.check_id = saved.get("check_id")
            if type(self.check_id) is not int or self.check_id <= 0:
                raise CodequiryError("Receipt has no valid provider check ID.")
        else:
            saved = {"digest": digest, "stage": "create_requested"}
            self._save(saved)
            self.check_id = self.client.create_check("pr-" + digest[:24], language=999, test_type=self.test_type)
            saved.update(check_id=self.check_id, stage="upload_requested")
            self._save(saved)
            uploaded = self.client.upload(self.check_id, archive)
            check = uploaded.get("check") if isinstance(uploaded, dict) else None
            if not isinstance(check, dict) or check.get("id") != self.check_id:
                raise CodequiryError("Upload response did not confirm the intended check; reconcile before retrying.")
            saved["stage"] = "start_requested"
            self._save(saved)
            self.client.start(self.check_id, test_type=self.test_type)
        while True:
            payload = self._read(lambda: self.client.get_check(self.check_id), deadline)
            check = payload.get("check") if isinstance(payload, dict) else None
            if not isinstance(check, dict) or check.get("id") != self.check_id:
                raise CodequiryError("Status belongs to an unexpected check.")
            status = check.get("status_id")
            if type(status) is not int or status not in (4, 5, 6, 7):
                raise CodequiryError("Check is not running; reconcile the earlier start request.")
            if status == 5:
                raise CodequiryError("Codequiry analysis failed.")
            if status == 4:
                break
            if time.monotonic() + 30 >= deadline:
                raise CodequiryError("Codequiry analysis deadline exceeded.")
            time.sleep(30)
        scores = component_scores(self._read(lambda: self.client.ai_results(self.check_id), deadline),
                                  self.check_id, mapping)
        saved["stage"] = "completed"
        self._save(saved)
        if self.delete_after:
            try:
                result = self.client.delete_check(self.check_id)
                if not isinstance(result, dict) or result.get("success") is not True:
                    raise CodequiryError("Provider did not confirm deletion.")
                saved["stage"] = "deleted"
                self._save(saved)
                self.cleanup = "deleted"
            except CodequiryError:
                self.cleanup = "failed"
        return scores


class CodequiryCorpusDetector:
    """One resumable receipt per unique submission in an authorized benchmark."""

    def __init__(self, client: CodequiryClient, test_type: int, directory: Path):
        self.client = client
        self.test_type = test_type
        self.directory = directory

    def score(self, components: tuple[Component, ...]) -> dict[str, Decimal]:
        archive, _ = package(components)
        identity = hashlib.sha256(archive + str(self.test_type).encode()).hexdigest()
        return CodequiryDetector(
            self.client, test_type=self.test_type, receipt=self.directory / (identity + ".json"),
            revision="benchmark-v1",
        ).score(components)
