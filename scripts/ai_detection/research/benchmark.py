"""Run the prespecified pilot or a provenance-labeled local JSONL corpus."""

import argparse
from collections import defaultdict
from decimal import Decimal
from fractions import Fraction
import hashlib
import json
from pathlib import Path
from time import perf_counter

from ..detector import Detector
from ..github_diff import read_manifest
from ..policy import ScorePolicy, evaluate
from .models import LocalClassifier, MODEL_HASHES


def load_samples(manifest: Path) -> list[dict]:
    samples = []
    ids = set()
    split_by_family = {}
    split_by_hash = {}
    split_by_source = {}
    root = manifest.resolve().parent
    for line in manifest.read_text(encoding="utf-8").splitlines():
        row = json.loads(line)
        for field in ("sample_id", "family_id", "variant", "provenance"):
            if not row.get(field):
                raise ValueError(f"Missing {field} in manifest")
        sample_id, split = row["sample_id"], row["split"]
        if sample_id in ids or split not in {"calibration", "holdout"}:
            raise ValueError(f"Duplicate sample or invalid split: {sample_id}")
        if row["label"] not in {"acceptable", "generated"}:
            raise ValueError(f"Invalid policy label for {sample_id}")
        ids.add(sample_id)
        path = (root / row["patch_path"]).resolve()
        if not path.is_relative_to(root):
            raise ValueError("Sample path escapes the corpus directory")
        content = path.read_bytes()
        digest = hashlib.sha256(content).hexdigest()
        if digest != row["patch_sha256"]:
            raise ValueError(f"Input checksum mismatch: {sample_id}")
        if (split_by_family.setdefault(row["family_id"], split) != split
                or split_by_hash.setdefault(digest, split) != split):
            raise ValueError(f"Family or input occurs on both sides of the split: {sample_id}")
        saved = json.loads(content)
        extracted = read_manifest(saved["before"], saved["pages"], saved["after"])
        # Changing a PR number, filename or context must not conceal identical
        # detector inputs across splits. Ignore manifests with no eligible text.
        inputs = sorted((c.language, c.block.lines) for c in extracted.components)
        if inputs:
            fingerprint = hashlib.sha256(json.dumps(inputs).encode()).digest()
            if split_by_source.setdefault(fingerprint, split) != split:
                raise ValueError(f"Identical detector input occurs in both splits: {sample_id}")
        samples.append({**row, "manifest": extracted})
    if {row["split"] for row in samples} != {"calibration", "holdout"}:
        raise ValueError("Both calibration and holdout samples are required")
    return samples


def _measure(samples: list[dict], detector: Detector) -> list[dict]:
    rows = []
    for sample in samples:
        inputs = sample["manifest"]
        measured = evaluate(inputs, detector=detector, policy=ScorePolicy(Decimal(100), 1))
        languages = sorted({component.language for component in inputs.components})
        rows.append({
            "sample_id": sample["sample_id"], "family_id": sample["family_id"], "split": sample["split"],
            "label": sample["label"], "variant": sample["variant"], "language": "+".join(languages),
            "patch_sha256": sample["patch_sha256"], "eligible": inputs.eligible_lines,
            "analyzed": measured.analyzed_lines, "score": measured.score,
            "outcome": measured.outcome.value,
        })
    return rows


def pilot_maximum(calibration: list[dict]) -> Decimal:
    if any(row["split"] != "calibration" for row in calibration):
        raise ValueError("Holdout data cannot set the maximum")
    scored = [row for row in calibration if row["score"] is not None]
    if {row["label"] for row in scored} != {"acceptable", "generated"}:
        raise ValueError("Need scored examples of both classes in calibration")
    maximum = max(row["score"] for row in scored if row["label"] == "acceptable")
    units = maximum * 1_000_000
    return Decimal(-(-units.numerator // units.denominator)).scaleb(-6)


def metrics(rows: list[dict]) -> dict:
    counts = dict(tp=0, fp=0, tn=0, fn=0, abstain_acceptable=0, abstain_generated=0)
    for row in rows:
        positive = row["label"] == "generated"
        if row["outcome"] == "FAIL":
            key = "tp" if positive else "fp"
        elif row["outcome"] == "PASS":
            key = "fn" if positive else "tn"
        else:
            key = "abstain_" + row["label"]
        counts[key] += 1
    tp, fp, tn, fn = (counts[k] for k in ("tp", "fp", "tn", "fn"))

    def ratio(numerator, denominator):
        return numerator / denominator if denominator else None

    return {
        **counts, "n": len(rows), "coverage": ratio(tp + fp + tn + fn, len(rows)),
        "fpr": ratio(fp, tn + fp), "fnr": ratio(fn, tp + fn),
        "precision": ratio(tp, tp + fp), "recall": ratio(tp, tp + fn),
        "recall_all": ratio(tp, tp + fn + counts["abstain_generated"]),
    }


def run(samples: list[dict], detector: Detector) -> dict:
    calibration = _measure([s for s in samples if s["split"] == "calibration"], detector)
    maximum = pilot_maximum(calibration)
    holdout = _measure([s for s in samples if s["split"] == "holdout"], detector)
    for row in calibration + holdout:
        if row["score"] is not None:
            row["outcome"] = "FAIL" if row["score"] > maximum else "PASS"
            row["score"] = str(Fraction(row["score"]))
    headline = [row for row in holdout if row["variant"] in {"original", "generated"}]
    groups = defaultdict(list)
    for row in headline:
        groups["language/" + row["language"]].append(row)
        lines = row["eligible"]
        band = "0" if not lines else "1-9" if lines < 10 else "10-29" if lines < 30 else "30+"
        groups["lines/" + band].append(row)
    for row in holdout:
        groups["variant/" + row["variant"]].append(row)
    return {"research_maximum": str(maximum), "headline": metrics(headline),
            "calibration": metrics(calibration), "holdout_all_variants": metrics(holdout),
            "holdout_groups": {key: metrics(rows) for key, rows in sorted(groups.items())},
            "samples": calibration + holdout}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--candidate", required=True, choices=MODEL_HASHES)
    parser.add_argument("--model-dir", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    if args.output.exists():
        parser.error("Choose a fresh output path to preserve the previous run")
    samples = load_samples(args.manifest)
    start = perf_counter()
    detector = LocalClassifier(args.candidate, args.model_dir)
    loaded = perf_counter()
    results = run(samples, detector)
    results.update(candidate=args.candidate, model_sha256=MODEL_HASHES[args.candidate]["model.safetensors"],
                   manifest_sha256=hashlib.sha256(args.manifest.read_bytes()).hexdigest(),
                   load_seconds=loaded-start, evaluate_seconds=perf_counter()-loaded)
    with args.output.open("x", encoding="utf-8") as out:
        json.dump(results, out, indent=2)
    print(json.dumps({"candidate": args.candidate, "research_maximum": results["research_maximum"],
                      "headline": results["headline"]}))


if __name__ == "__main__":
    main()
