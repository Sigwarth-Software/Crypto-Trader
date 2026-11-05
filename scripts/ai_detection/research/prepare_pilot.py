"""Build the issue-134 corpus from pinned public releases and recorded AI outputs."""

import argparse
import hashlib
import json
from pathlib import Path
from urllib.parse import quote
from urllib.request import urlopen


ARCHIVES = {
    "Python": ("TheAlgorithms/Python", "34c808b3751f3f9b0fe45e736b2f61abee1b91f6"),
    "Java": ("TheAlgorithms/Java", "01283e3ac1821600312ce4880ab2bec551e879d3"),
    "Kotlin": ("TheAlgorithms/Kotlin", "b585cc765f1f6056c1a659f786c8dfdde3e8cbf8"),
    "Javascript": ("TheAlgorithms/Javascript", "0b544c66c59b0c02b8037e66d0b6ce27e65e7a83"),
    "TypeScript": ("denoland/deno_std", "73671a6f04d288dd57e75f9806959f7b055ae4a7"),
}


def fixture(files: dict[str, str], context: str | None = None) -> bytes:
    """Wrap additions as GitHub-style files. Context models a modified-file hunk."""
    entries = []
    physical = 0
    for path, source in files.items():
        lines = source.splitlines()
        physical += len(lines)
        if context is None:
            header = f"@@ -0,0 +1,{len(lines)} @@"
            body = [header]
        else:
            body = [f"@@ -1 +1,{len(lines) + 1} @@", " " + context]
        body.extend("+" + line for line in lines)
        entries.append({"filename": path, "status": "added" if context is None else "modified",
                        "additions": len(lines), "deletions": 0, "patch": "\n".join(body)})
    snapshot = {"number": 1, "changed_files": len(files), "additions": physical, "deletions": 0}
    for side, ref, char in (("base", "development", "a"), ("head", "experiment", "b")):
        snapshot[side] = {"repo": {"id": 1}, "ref": ref, "sha": char * 40}
    return json.dumps({"before": snapshot, "pages": [entries], "after": snapshot},
                      ensure_ascii=False, sort_keys=True).encode("utf-8")


def prepare(directory: Path) -> None:
    directory.mkdir(parents=True, exist_ok=True)
    # Fail before fetching if this location already contains a completed manifest.
    manifest_path = directory / "manifest.jsonl"
    if manifest_path.exists():
        raise ValueError("Choose a new corpus directory to preserve the previous run.")
    records = []
    tasks = Path(__file__).with_name("pilot_tasks.jsonl").read_text(encoding="utf-8").splitlines()
    for line in tasks:
        task = json.loads(line)
        archive, revision = ARCHIVES[task["archive"]]
        url = f"https://raw.githubusercontent.com/{archive}/{revision}/{quote(task['path'])}"
        with urlopen(url, timeout=20) as response:
            original = response.read(256_001)
        if len(original) > 256_000:
            raise ValueError("Unexpectedly large pilot source")
        historical = original.decode("utf-8-sig")
        generated = task["generated"]
        path = Path(task["path"])
        family = f"{task['archive']}-{path.stem}"
        comment = "#" if path.suffix == ".py" else "//"
        variants = [
            ("original", "acceptable", {path.name: historical}, None),
            ("generated", "generated", {path.name: generated}, None),
            ("light_assist", "acceptable", {path.name: historical + f"\n{comment} End of implementation."}, None),
            ("mixed", "generated", {"legacy/" + path.name: historical, "new/" + path.name: generated}, None),
            ("reformat", "generated", {path.name: generated.replace("\n", "\n\n")}, None),
            ("context", "generated", {path.name: generated}, f"{comment} Existing source context"),
        ]
        for variant, label, files, context in variants:
            sample_id = f"{family}-{variant}"
            filename = sample_id + ".json"
            content = fixture(files, context)
            (directory / filename).write_bytes(content)
            records.append({"sample_id": sample_id, "family_id": family, "split": task["split"],
                            "variant": variant, "label": label, "patch_path": filename,
                            "patch_sha256": hashlib.sha256(content).hexdigest(),
                            "provenance": {"historical_source": url, "original_sha256": hashlib.sha256(original).hexdigest(),
                                           "generator": "Codex GPT-6, issue-134 pilot, 2026-11-05"}})
    manifest_path.write_bytes("".join(json.dumps(record) + "\n" for record in records).encode())


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    prepare(parser.parse_args().directory)
