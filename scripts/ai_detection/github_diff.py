"""Read saved GitHub PR metadata and file-list responses without executing PR code."""

from collections.abc import Mapping, Sequence
import re

from .diff import AddedBlock, Manifest, build_manifest


class IncompleteDiff(ValueError):
    """The supplied responses cannot establish the complete set of additions."""


_HUNK = re.compile(
    r"@@ -([0-9]{1,12})(?:,([0-9]{1,12}))? "
    r"\+([0-9]{1,12})(?:,([0-9]{1,12}))? @@(?: .*)?"
)
_NO_NEWLINE = r"\ No newline at end of file"
FILE_LIMIT = 3000


def _count(record: Mapping, name: str) -> int:
    value = record.get(name)
    if type(value) is not int or value < 0:
        raise IncompleteDiff(f"Invalid or missing {name} count.")
    return value


def _revision(pr: Mapping) -> tuple:
    identity = []
    for side in ("base", "head"):
        ref = pr.get(side)
        if not isinstance(ref, Mapping) or not isinstance(ref.get("repo"), Mapping):
            raise IncompleteDiff(f"Missing {side} repository metadata.")
        sha = ref.get("sha")
        branch = ref.get("ref")
        if not isinstance(sha, str) or not re.fullmatch(r"[0-9a-f]{40}", sha):
            raise IncompleteDiff(f"Invalid {side} revision.")
        if not isinstance(branch, str) or not branch:
            raise IncompleteDiff(f"Missing {side} branch.")
        identity.append((_count(ref["repo"], "id"), branch, sha))
    return _count(pr, "number"), *identity


def _range(start: str, length: str | None) -> tuple[int, int]:
    """Convert a unified range to a zero-based, half-open interval."""
    count = int(length) if length is not None else 1
    offset = int(start) - (1 if count else 0)
    if offset < 0:
        raise IncompleteDiff("Invalid hunk line number.")
    return offset, offset + count


def _read_patch(path: str, patch: str, additions: int, deletions: int) -> list[AddedBlock]:
    # Only LF separates patch records. A Unicode line separator can be source text.
    lines = patch.removesuffix("\n").split("\n")
    blocks = []
    old_cursor = new_cursor = 0
    added = deleted = 0
    index = 0
    while index < len(lines):
        header = _HUNK.fullmatch(lines[index].removesuffix("\r"))
        if header is None:
            raise IncompleteDiff("Invalid unified hunk header.")
        old_start, old_end = _range(header[1], header[2])
        new_start, new_end = _range(header[3], header[4])
        old_gap = old_start - old_cursor
        if old_gap < 0 or old_gap != new_start - new_cursor:
            raise IncompleteDiff("Overlapping or inconsistent hunk positions.")
        old_cursor, new_cursor = old_start, new_start
        index += 1
        pending: list[str] = []
        previous_was_data = False
        while index < len(lines) and not lines[index].startswith("@@"):
            line = lines[index].removesuffix("\r")
            index += 1
            if line == _NO_NEWLINE and previous_was_data:
                previous_was_data = False
                continue
            previous_was_data = True
            prefix = line[:1]
            if prefix == "+":
                pending.append(line[1:])
                new_cursor += 1
                added += 1
            elif prefix == "-":
                old_cursor += 1
                deleted += 1
            elif prefix == " ":
                if pending:
                    blocks.append(AddedBlock(path, new_cursor - len(pending) + 1, tuple(pending)))
                    pending.clear()
                old_cursor += 1
                new_cursor += 1
            else:
                raise IncompleteDiff("Invalid unified hunk body.")
        if (old_cursor, new_cursor) != (old_end, new_end):
            raise IncompleteDiff("Hunk line counts do not match the header.")
        if pending:
            blocks.append(AddedBlock(path, new_cursor - len(pending) + 1, tuple(pending)))
    if (added, deleted) != (additions, deletions):
        raise IncompleteDiff("Patch totals do not match the file metadata.")
    return blocks


def read_manifest(before: Mapping, file_pages: Sequence[list], after: Mapping) -> Manifest:
    """Use GET /pulls/{number} snapshots bracketing all /files response pages."""
    if not isinstance(before, Mapping) or not isinstance(after, Mapping):
        raise IncompleteDiff("Missing pull request metadata.")
    if _revision(before) != _revision(after):
        raise IncompleteDiff("Pull request revision changed while reading files.")
    count_fields = ("changed_files", "additions", "deletions")
    totals = tuple(_count(before, key) for key in count_fields)
    if totals != tuple(_count(after, key) for key in count_fields):
        raise IncompleteDiff("Pull request totals changed while reading files.")
    if totals[0] > FILE_LIMIT:
        raise IncompleteDiff("The PR exceeds the GitHub files API limit.")
    if not isinstance(file_pages, Sequence) or isinstance(file_pages, (str, bytes, bytearray)):
        raise IncompleteDiff("Missing files pages.")

    paths: set[str] = set()
    blocks: list[AddedBlock] = []
    added = deleted = 0
    for page in file_pages:
        if not isinstance(page, list):
            raise IncompleteDiff("Invalid files page.")
        for entry in page:
            if not isinstance(entry, Mapping):
                raise IncompleteDiff("Invalid file metadata.")
            path = entry.get("filename")
            if not isinstance(path, str) or not path or path in paths:
                raise IncompleteDiff("Missing or duplicate destination filename.")
            paths.add(path)
            if len(paths) > totals[0]:
                raise IncompleteDiff("More files than reported by the pull request.")
            file_added = _count(entry, "additions")
            file_deleted = _count(entry, "deletions")
            status = entry.get("status")
            if (status == "removed" and file_added) or (status == "added" and file_deleted):
                raise IncompleteDiff("File status contradicts its line counts.")
            patch = entry.get("patch")
            if isinstance(patch, str) and patch:
                blocks.extend(_read_patch(path, patch, file_added, file_deleted))
            elif file_added or (patch is not None and patch != ""):
                raise IncompleteDiff("Missing or invalid patch for a changed file.")
            # A patchless binary, pure rename, or deletion can contribute zero additions.
            added += file_added
            deleted += file_deleted
    if (len(paths), added, deleted) != totals:
        raise IncompleteDiff("File list does not match the pull request totals.")
    return build_manifest(tuple(blocks))
