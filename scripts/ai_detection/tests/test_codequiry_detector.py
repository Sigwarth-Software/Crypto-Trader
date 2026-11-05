from contextlib import redirect_stdout, redirect_stderr
from decimal import Decimal
from io import BytesIO, StringIO
import json
import shlex
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest
from unittest.mock import Mock, patch
from zipfile import ZipFile

from scripts.ai_detection.codequiry import CodequiryClient, CodequiryError
from scripts.ai_detection.codequiry_detector import CodequiryDetector, CodequiryCorpusDetector, package
from scripts.ai_detection.codequiry_run import main
from scripts.ai_detection.diff import AddedBlock, build_manifest
from scripts.ai_detection.policy import ScorePolicy, Outcome, evaluate


class CodequiryDetectorTests(unittest.TestCase):
    def setUp(self):
        self.directory = Path(self.enterContext(TemporaryDirectory()))
        self.manifest = build_manifest((AddedBlock("private/internal.py", 40, ("a", "b")),
                                        AddedBlock("private/lib.java", 80, ("c",))))
        self.client = Mock(spec=CodequiryClient)
        self.client.create_check.return_value = 42
        self.client.upload.return_value = {"check": {"id": 42}, "submission_count": 1}
        self.client.get_check.return_value = {"check": {"id": 42, "status_id": 4}}
        self.client.ai_results.return_value = self.result()
        self.detector = self.make_detector()

    def make_detector(self, **kwargs):
        return CodequiryDetector(self.client, test_type=9, receipt=self.directory / "receipt.json",
                                 revision="base:head", **kwargs)

    def result(self):
        _, mapping = package(self.manifest.components)
        return {"success": True, "assignment_id": 42, "assignment_status": 4,
                "submissions": [{"status_id": 4, "ai_detection": {"total_files": 2, "files": [
                    {"file_path": name, "ai_probability": Decimal(50)} for name in mapping]}}]}

    def test_package_is_deterministic_and_hides_original_paths(self):
        archive, mapping = package(self.manifest.components)
        self.assertEqual(package(self.manifest.components), (archive, mapping))
        with ZipFile(BytesIO(archive)) as saved:
            self.assertEqual(set(saved.namelist()), set(mapping))
            self.assertNotIn(b"private", archive)
            self.assertEqual(saved.read(next(iter(mapping))), b"a\nb")

    def test_full_lifecycle_scores_and_resumes_without_mutations(self):
        result = evaluate(self.manifest, detector=self.detector, policy=ScorePolicy(Decimal(50), 1))
        self.assertEqual((result.outcome, result.score, result.analyzed_lines), (Outcome.PASS, 50, 3))
        self.make_detector().score(self.manifest.components)
        self.client.create_check.assert_called_once()
        self.client.upload.assert_called_once()
        self.client.start.assert_called_once()

    def test_missing_duplicate_unexpected_or_invalid_score_is_inconclusive(self):
        for change in ("missing", "duplicate", "unexpected", "invalid", "wrong_job"):
            with self.subTest(change=change), TemporaryDirectory() as directory:
                detector = CodequiryDetector(self.client, test_type=9, receipt=Path(directory) / "r", revision="r")
                data = self.result()
                files = data["submissions"][0]["ai_detection"]["files"]
                if change == "missing":
                    files.pop()
                elif change == "duplicate":
                    files[1] = files[0]
                elif change == "unexpected":
                    files[0]["file_path"] = "../other.py"
                elif change == "invalid":
                    files[0]["ai_probability"] = Decimal("NaN")
                else:
                    data["assignment_id"] = 999
                self.client.ai_results.return_value = data
                decision = evaluate(self.manifest, detector=detector, policy=ScorePolicy(Decimal(50), 1))
                self.assertEqual(decision.outcome, Outcome.INCONCLUSIVE)
                self.assertIsNone(decision.score)

    def test_ambiguous_upload_cannot_be_replayed(self):
        self.client.upload.side_effect = CodequiryError("Connection failed")
        with self.assertRaises(CodequiryError):
            self.detector.score(self.manifest.components)
        with self.assertRaisesRegex(CodequiryError, "ambiguous"):
            self.make_detector().score(self.manifest.components)
        self.client.create_check.assert_called_once()
        self.client.upload.assert_called_once()

    def test_wrong_upload_receipt_prevents_start(self):
        self.client.upload.return_value = {"check": {"id": 99}}
        with self.assertRaisesRegex(CodequiryError, "intended check"):
            self.detector.score(self.manifest.components)
        self.client.start.assert_not_called()

    def test_changed_input_or_locked_receipt_stops_before_network(self):
        self.detector.score(self.manifest.components)
        self.client.reset_mock()
        self.detector.revision = "another-head"
        with self.assertRaisesRegex(CodequiryError, "different inputs"):
            self.detector.score(self.manifest.components)
        self.client.get_check.assert_not_called()
        (self.directory / "receipt.json.lock").touch()
        with self.assertRaisesRegex(CodequiryError, "locked"):
            self.detector.score(self.manifest.components)
        self.client.create_check.assert_not_called()

    def test_polling_rate_limit_and_deadline(self):
        self.client.get_check.side_effect = [
            CodequiryError("Rate limit", status=429, retry_after=30),
            {"check": {"id": 42, "status_id": 7}},
            {"check": {"id": 42, "status_id": 4}},
        ]
        with patch("scripts.ai_detection.codequiry_detector.time.sleep") as sleep:
            self.detector.score(self.manifest.components)
            self.assertEqual(sleep.call_count, 2)
        self.client.get_check.side_effect = None
        self.client.get_check.return_value = {"check": {"id": 42, "status_id": 6}}
        with self.assertRaisesRegex(CodequiryError, "deadline"):
            self.make_detector(timeout=1).score(self.manifest.components)

    def test_cleanup_failure_is_reported_without_losing_scores(self):
        detector = self.make_detector(delete_after=True)
        self.client.delete_check.side_effect = CodequiryError("Deletion unavailable")
        self.assertEqual(len(detector.score(self.manifest.components)), 2)
        self.assertEqual(detector.cleanup, "failed")
        self.client.delete_check.assert_called_once_with(42)

    def test_failed_or_wrong_status_never_fetches_results(self):
        for check in ({"id": 42, "status_id": 5}, {"id": 99, "status_id": 4}):
            with self.subTest(check=check), TemporaryDirectory() as directory:
                self.client.reset_mock()
                self.client.get_check.return_value = {"check": check}
                detector = CodequiryDetector(self.client, test_type=9, receipt=Path(directory) / "r", revision="r")
                with self.assertRaises(CodequiryError):
                    detector.score(self.manifest.components)
                self.client.ai_results.assert_not_called()

    def test_benchmark_reuses_completed_submission(self):
        detector = CodequiryCorpusDetector(self.client, 9, self.directory)
        self.assertEqual(detector.score(self.manifest.components), detector.score(self.manifest.components))
        self.client.start.assert_called_once()

    def test_cli_requires_explicit_authorization_before_any_client(self):
        with patch("scripts.ai_detection.codequiry_run.CodequiryClient") as client:
            with redirect_stderr(StringIO()), self.assertRaises(SystemExit):
                main(["42", "--engine-id", "9", "--receipt", str(self.directory / "r")])
            client.assert_not_called()

    def test_workflow_command_is_accepted_by_the_cli(self):
        workflow = Path(__file__).parents[3] / ".github/workflows/codequiry-evaluation.yml"
        command = next(line.strip() for line in workflow.read_text().splitlines()
                       if line.strip().startswith("python -m scripts.ai_detection.codequiry_run"))
        argv = shlex.split(command)[3:]
        replacements = {"$PR_NUMBER": "42", "$CODEQUIRY_ENGINE_ID": "9",
                        "$RUNNER_TEMP/codequiry/receipt.json": str(self.directory / "job.json"),
                        "$GITHUB_STEP_SUMMARY": str(self.directory / "summary.md")}
        argv = [replacements.get(value, value) for value in argv if value != "${options[@]}"]
        env = {"CODEQUIRY_API_KEY": "fake-test-key", "AI_DETECTION_MAX_SCORE": "100",
               "AI_DETECTION_MIN_LINES": "1"}
        with patch.dict("os.environ", env), patch("scripts.ai_detection.codequiry_run.PullRequestReader") as reader:
            reader.return_value.snapshot.return_value = {"base": {"sha": "a"}, "head": {"sha": "b"}}
            with patch("scripts.ai_detection.codequiry_run.inspect", return_value={"outcome": "OUT_OF_SCOPE"}):
                with redirect_stdout(StringIO()):
                    self.assertEqual(main(argv), 0)
        self.assertTrue((self.directory / "summary.md").exists())

    def test_cli_reports_paid_result_and_checks_latest_revision(self):
        fixture = json.loads((Path(__file__).parent / "fixtures/pr_files.json").read_text())
        before = fixture["pull_request"]
        before["head"]["repo"]["owner"] = {"id": 279793999}
        before["head"]["sha"] = "a" * 40
        reader = Mock()
        reader.snapshot.return_value = before
        reader.manifest.return_value = self.manifest
        env = {"CODEQUIRY_API_KEY": "fake-test-key", "AI_DETECTION_MAX_SCORE": "50", "AI_DETECTION_MIN_LINES": "1"}
        with patch.dict("os.environ", env), patch("scripts.ai_detection.codequiry_run.CodequiryClient", return_value=self.client):
            with patch("scripts.ai_detection.codequiry_run.PullRequestReader", return_value=reader):
                output = StringIO()
                with redirect_stdout(output):
                    self.assertEqual(main(["42", "--engine-id", "9", "--receipt",
                                           str(self.directory / "receipt.json"), "--authorize-upload-and-charges"]), 0)
                report = json.loads(output.getvalue())
                self.assertEqual(report["outcome"], "PASS")
                self.assertEqual(report["provider_check_id"], 42)
                self.assertEqual(report["exact_score"], "50")
                self.assertNotIn("fake-test-key", output.getvalue())
