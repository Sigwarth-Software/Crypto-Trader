from decimal import Decimal
from fractions import Fraction
import hashlib
import json
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

from scripts.ai_detection.research.benchmark import load_samples, metrics, pilot_maximum, run
from scripts.ai_detection.research.prepare_pilot import fixture


class BenchmarkTests(unittest.TestCase):
    def setUp(self):
        directory = self.enterContext(TemporaryDirectory())
        self.root = Path(directory)
        self.path = self.root / "manifest.jsonl"
        self.rows = []
        for split in ("calibration", "holdout"):
            for variant, label in (("original", "acceptable"), ("generated", "generated")):
                sample_id = f"{split}-{variant}"
                content = fixture({"file.py": sample_id})
                patch = self.root / f"{sample_id}.json"
                patch.write_bytes(content)
                self.rows.append(dict(sample_id=sample_id, family_id=split, split=split,
                                      variant=variant, label=label, provenance="test",
                                      patch_path=patch.name, patch_sha256=hashlib.sha256(content).hexdigest()))

    def load(self):
        self.path.write_text("\n".join(map(json.dumps, self.rows)), encoding="utf-8")
        return load_samples(self.path)

    def test_replays_actual_diff_and_separates_calibration(self):
        class FakeDetector:
            def score(self, components):
                values = {"calibration-original": 41, "calibration-generated": 80,
                          "holdout-original": 60, "holdout-generated": 20}
                return {c.component_id: Decimal(values[c.block.lines[0]]) for c in components}

        result = run(self.load(), FakeDetector())
        self.assertEqual(Decimal(result["research_maximum"]), Decimal(41))
        self.assertEqual(result["headline"]["fp"], 1)
        self.assertEqual(result["headline"]["fn"], 1)
        self.assertEqual(result["headline"]["coverage"], 1)

    def test_duplicate_family_and_content_cannot_cross_splits(self):
        self.rows[2]["family_id"] = "calibration"
        with self.assertRaisesRegex(ValueError, "both sides"):
            self.load()
        self.rows[2]["family_id"] = "another"
        for key in ("patch_path", "patch_sha256"):
            self.rows[2][key] = self.rows[0][key]
        with self.assertRaisesRegex(ValueError, "both sides"):
            self.load()

    def test_inputs_are_bound_to_digest(self):
        self.rows[0]["patch_sha256"] = "0" * 64
        with self.assertRaisesRegex(ValueError, "checksum"):
            self.load()

    def test_same_source_with_different_diff_metadata_cannot_cross_splits(self):
        row = self.rows[2]
        content = fixture({"renamed.py": "calibration-original"}, "# existing context")
        (self.root / row["patch_path"]).write_bytes(content)
        row["patch_sha256"] = hashlib.sha256(content).hexdigest()
        with self.assertRaisesRegex(ValueError, "Identical detector input"):
            self.load()

    def test_calibration_rounds_up_and_rejects_holdout(self):
        calibration = [{"split": "calibration", "score": Fraction(1, 3), "label": "acceptable"},
                       {"split": "calibration", "score": Fraction(2), "label": "generated"}]
        self.assertEqual(pilot_maximum(calibration), Decimal("0.333334"))
        calibration[1]["split"] = "holdout"
        with self.assertRaisesRegex(ValueError, "Holdout"):
            pilot_maximum(calibration)

    def test_abstentions_and_empty_denominators(self):
        stats = metrics([{"outcome": "INCONCLUSIVE", "label": "generated"},
                         {"outcome": "PASS", "label": "acceptable"}])
        self.assertIsNone(stats["recall"])
        self.assertIsNone(stats["precision"])
        self.assertEqual(stats["coverage"], 0.5)
        self.assertEqual(stats["recall_all"], 0)
