from copy import deepcopy
from decimal import Decimal
import json
from pathlib import Path
import unittest
from unittest.mock import Mock

from scripts.ai_detection.detector import Detector
from scripts.ai_detection.github_diff import read_manifest
from scripts.ai_detection.policy import Outcome, ScorePolicy, evaluate_diff


FIXTURE = Path(__file__).parent / "fixtures" / "pr_files.json"


class PullRequestDiffTests(unittest.TestCase):
    def setUp(self):
        fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.before = fixture["pull_request"]
        self.after = deepcopy(self.before)
        self.pages = fixture["pages"]
        self.detector = Mock(spec=Detector)
        self.policy = ScorePolicy(Decimal("50"), minimum_lines=1)

    def decision(self):
        return evaluate_diff(self.before, self.pages, self.after, detector=self.detector, policy=self.policy)

    def assert_incomplete(self, expected_reason):
        result = self.decision()
        self.assertEqual(result.outcome, Outcome.INCONCLUSIVE)
        self.assertIn(expected_reason, result.reason)
        self.assertIsNone(result.score)
        self.detector.score.assert_not_called()

    def test_extracts_unique_head_ranges_across_pages(self):
        manifest = read_manifest(self.before, self.pages, self.after)
        self.assertEqual(manifest.physical_lines, 7)
        self.assertEqual(manifest.eligible_lines, 4)
        ranges = [(c.block.path, c.block.first_line, c.block.lines) for c in manifest.components]
        self.assertEqual(ranges, [
            ("src/New.kt", 5, ("new()", "+counter")),
            ("src/New.kt", 17, ("finish()",)),
            ("tests/case.py", 1, ("assert True", "")),
        ])
        self.detector.score.return_value = {"1": Decimal(40), "2": Decimal(20), "3": Decimal(100)}
        result = self.decision()
        self.assertEqual((result.outcome, result.score, result.analyzed_lines), (Outcome.PASS, 50, 4))

    def test_file_list_missing_a_page(self):
        self.pages.pop()
        self.assert_incomplete("File list")

    def test_equal_page_length_with_repeated_file(self):
        self.pages[1][0] = self.pages[0][2]
        self.assert_incomplete("duplicate")

    def test_change_during_pagination_even_with_identical_totals(self):
        for side, key in (("head", "sha"), ("base", "sha"), ("base", "ref")):
            with self.subTest(side=side, key=key):
                self.after = deepcopy(self.before)
                self.after[side][key] = "c" * 40
                self.assert_incomplete("revision changed")

    def test_inconsistent_totals_with_stable_shas(self):
        self.after["additions"] += 1
        self.assert_incomplete("totals changed")
        self.before["additions"] += 1
        self.assert_incomplete("File list")

    def test_cap_and_invalid_metadata(self):
        for count, reason in ((3001, "API limit"), (False, "count"), (-1, "count")):
            with self.subTest(count=count):
                self.before["changed_files"] = self.after["changed_files"] = count
                self.assert_incomplete(reason)

    def test_missing_patch_even_when_only_excluded_lines_are_missing(self):
        del self.pages[1][-1]["patch"]
        self.assert_incomplete("Missing or invalid patch")

    def test_missing_or_malformed_response(self):
        for pages in (None, [{}], [[None]]):
            with self.subTest(pages=pages):
                self.pages = pages
                self.assert_incomplete("file")

    def test_removed_file_cannot_contribute_additions(self):
        self.pages[0][1]["status"] = "removed"
        self.assert_incomplete("status contradicts")

    def test_hunk_integrity(self):
        row = self.pages[0][0]
        intact = row["patch"]
        malformed = (
            (intact.replace("+4,4", "+4,5"), "line counts"),
            (intact.partition("\n@@ -15")[0], "Patch totals"),
            (intact.replace("@@ -15 +16", "@@ -15 +15"), "positions"),
            (intact.replace("@@ -15 +16", "@@ -4 +5"), "positions"),
            (intact.replace(" end()", "end()"), "hunk body"),
            (intact.replace("@@ -4,3", "@@ broken"), "header"),
        )
        for patch, reason in malformed:
            with self.subTest(reason=reason):
                row["patch"] = patch
                self.assert_incomplete(reason)

    def test_raw_lines_are_preserved_with_crlf_and_marker_like_text(self):
        self.pages[0][1]["patch"] = "@@ -0,0 +1,2 @@\r\n+++content\r\n+left\u2028right\r\n"
        manifest = read_manifest(self.before, self.pages, self.after)
        self.assertEqual(manifest.components[-1].block.lines, ("++content", "left\u2028right"))

    def test_zero_length_hunk_ranges_after_existing_lines(self):
        self.pages[0][0]["patch"] = "@@ -2,0 +3,2 @@\n+x\n+y\n@@ -5 +6,0 @@\n-z\n@@ -7,0 +9 @@\n+w"
        manifest = read_manifest(self.before, self.pages, self.after)
        self.assertEqual([c.block.first_line for c in manifest.components[:2]], [3, 9])

    def test_no_eligible_code_requires_complete_listing(self):
        self.pages = [[self.pages[0][2], self.pages[1][0]]]
        for snapshot in (self.before, self.after):
            snapshot.update(changed_files=2, additions=0, deletions=0)
        self.assertEqual(self.decision().outcome, Outcome.NO_ELIGIBLE_CODE)
        self.pages = []
        self.assert_incomplete("File list")
