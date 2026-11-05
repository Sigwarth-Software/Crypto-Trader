"""Download a pinned public checkpoint for the offline screening experiment."""

import argparse
import hashlib
from pathlib import Path
import shutil
from tempfile import TemporaryDirectory
from urllib.request import urlopen

from .models import MODEL_HASHES


RELEASES = {
    "VKR_Model": "AggressiveBag/VKR_Model/resolve/aa7f2736b507551cf3fc562702c04bff666f6693",
    "GraphCodeBERT": (
        "dzungpham/graphcodebert-code-classification/resolve/"
        "34959f8150aa72974f031232b8d5f8b74b50e9fe/graphcodebert-vanilla/final_model"
    ),
}


def stage(candidate: str, directory: Path) -> None:
    directory.mkdir(exist_ok=True, parents=True)
    for name, expected in MODEL_HASHES[candidate].items():
        destination = directory / name
        if destination.exists():
            with destination.open("rb") as source:
                if hashlib.file_digest(source, "sha256").hexdigest() == expected:
                    continue
            raise ValueError(f"Existing file differs from the pinned release: {name}")
        url = f"https://huggingface.co/{RELEASES[candidate]}/{name}"
        with TemporaryDirectory(dir=directory) as scratch:
            staging = Path(scratch) / name
            with urlopen(url, timeout=60) as response, staging.open("wb") as out:
                shutil.copyfileobj(response, out)
            with staging.open("rb") as source:
                if hashlib.file_digest(source, "sha256").hexdigest() != expected:
                    raise ValueError(f"Downloaded file failed checksum verification: {name}")
            staging.replace(destination)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("candidate", choices=RELEASES)
    parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    stage(args.candidate, args.directory)
