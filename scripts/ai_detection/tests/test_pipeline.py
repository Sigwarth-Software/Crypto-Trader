from decimal import Decimal
from fractions import Fraction
import unittest
from unittest.mock import Mock

from scripts.ai_detection.detector import Detector, DetectorUnavailable
from scripts.ai_detection.diff import AddedBlock, build_manifest
from scripts.ai_detection.policy import Outcome, ScorePolicy, evaluate


class ManifestTests(unittest.TestCase):
    def test_candidates_preserve_source_and_count_nonblank_additions(self):
        paths = {
            "src/Main.java": "java",
            "test/MainTest.kt": "kotlin",
            "scripts/check.kts": "kotlin",
            "service.py": "python",
            "web/ui.tsx": "typescript",
            "web/app.js": "javascript",
        }
        blocks = tuple(AddedBlock(path, 10, ("first", " ", "last")) for path in paths)
        manifest = build_manifest(blocks)

        self.assertEqual(manifest.physical_lines, 18)
        self.assertEqual(manifest.eligible_lines, 12)
        self.assertEqual({c.block.path: c.language for c in manifest.components}, paths)
        self.assertTrue(all(c.block.lines == ("first", " ", "last") for c in manifest.components))
        self.assertEqual(build_manifest(tuple(reversed(blocks))), manifest)

    def test_exclusions_still_count_as_physical_additions(self):
        paths = ("README.md", "package-lock.json", "lib/node_modules/a.ts", "target/A.java",
                 "build/generated/A.kt", "vendor/lib.js")
        blocks = tuple(AddedBlock(path, 1, ("excluded",)) for path in paths)
        manifest = build_manifest(blocks + (AddedBlock("blank.py", 3, (" ",)),))
        self.assertEqual(manifest.physical_lines, 7)
        self.assertEqual(manifest.eligible_lines, 0)
        self.assertEqual(evaluate(manifest).outcome, Outcome.NO_ELIGIBLE_CODE)

    def test_ranges_in_one_file_must_be_disjoint(self):
        first = AddedBlock("Example.kt", 3, ("a", "b"))
        for start in (0, 3, 4):
            with self.subTest(start=start), self.assertRaises(ValueError):
                build_manifest((first, AddedBlock("Example.kt", start, ("c",))))
        manifest = build_manifest((first, AddedBlock("Example.kt", 15, ("c",))))
        self.assertEqual(manifest.eligible_lines, 3)
        self.assertEqual(len(manifest.components), 2)

    def test_embedded_newline_cannot_count_as_one_physical_line(self):
        with self.assertRaises(ValueError):
            build_manifest((AddedBlock("sample.py", 1, ("one\ntwo",)),))


class DecisionTests(unittest.TestCase):
    def setUp(self):
        self.manifest = build_manifest((
            AddedBlock("A.java", 1, ("line",) * 8),
            AddedBlock("B.kt", 5, ("line",) * 2),
        ))
        self.detector = Mock(spec=Detector)
        self.policy = ScorePolicy(maximum=Decimal("50"), minimum_lines=10)

    def evaluate(self):
        return evaluate(self.manifest, detector=self.detector, policy=self.policy)

    def test_weighted_score_compares_before_rounding(self):
        for small_score, expected, outcome in (
            ("89.999", Fraction(249999, 5000), Outcome.PASS),
            ("90", Fraction(50), Outcome.PASS),
            ("90.001", Fraction(250001, 5000), Outcome.FAIL),
        ):
            with self.subTest(small_score=small_score):
                self.detector.score.return_value = {"1": Decimal("40"), "2": Decimal(small_score)}
                decision = self.evaluate()
                self.assertEqual((decision.score, decision.outcome), (expected, outcome))
                self.assertEqual(decision.analyzed_lines, 10)

    def test_incomplete_diff_and_small_changes_skip_detector(self):
        for manifest in (None, build_manifest((AddedBlock("a.py", 1, ("one",)),))):
            with self.subTest(manifest=manifest):
                result = evaluate(manifest, detector=self.detector, policy=self.policy)
                self.assertEqual(result.outcome, Outcome.INCONCLUSIVE)
                self.assertIsNone(result.score)
        self.detector.score.assert_not_called()

    def test_unconfigured_pipeline_abstains(self):
        self.assertEqual(evaluate(self.manifest).outcome, Outcome.INCONCLUSIVE)

    def test_incomplete_responses_retain_known_coverage(self):
        for second in (None, Decimal("NaN"), Decimal("Infinity"), Decimal("101"), Decimal("-1")):
            scores = {"1": Decimal("40")}
            if second is not None:
                scores["2"] = second
            with self.subTest(second=second):
                self.detector.score.return_value = scores
                result = self.evaluate()
                self.assertEqual(result.outcome, Outcome.INCONCLUSIVE)
                self.assertIsNone(result.score)
                self.assertEqual(result.analyzed_lines, 8)

    def test_unexpected_result_id_invalidates_response(self):
        self.detector.score.return_value = {"other": Decimal("90")}
        result = self.evaluate()
        self.assertEqual(result.outcome, Outcome.INCONCLUSIVE)
        self.assertIsNone(result.score)

    def test_detector_outage_is_distinct_from_programming_error(self):
        self.detector.score.side_effect = DetectorUnavailable()
        self.assertEqual(self.evaluate().outcome, Outcome.INCONCLUSIVE)
        self.detector.score.side_effect = RuntimeError("broken adapter")
        with self.assertRaises(RuntimeError):
            self.evaluate()

    def test_policy_rejects_invalid_values(self):
        for maximum in ("NaN", "-1", "Infinity", "101"):
            with self.subTest(maximum=maximum), self.assertRaises(ValueError):
                ScorePolicy(Decimal(maximum), 1)
        for minimum in (0, True, 1.5):
            with self.subTest(minimum=minimum), self.assertRaises(ValueError):
                ScorePolicy(Decimal("50"), minimum)
