import json
import unittest
from unittest.mock import patch

from scripts.ai_detection.github_check import CheckPublishError, publish


class GitHubCheckTests(unittest.TestCase):
    def setUp(self):
        self.https = self.enterContext(patch("scripts.ai_detection.github_check.HTTPSConnection"))
        self.connection = self.https.return_value
        self.connection.getresponse.return_value.status = 201
        self.sha = "a" * 40

    def test_classifications_publish_on_analyzed_commit(self):
        for outcome, conclusion in (("PASS", "success"), ("FAIL", "failure"),
                                    ("INCONCLUSIVE", "neutral"), ("FORK_EXEMPT", "neutral"),
                                    ("NO_ELIGIBLE_CODE", "success")):
            with self.subTest(outcome=outcome):
                publish({"outcome": outcome, "head_sha": self.sha}, "fake-test-token", head_sha=self.sha)
                args, kwargs = self.connection.request.call_args
                self.assertEqual(args, ("POST", "/repos/Sigwarth-Software/Crypto-Trader/check-runs"))
                payload = json.loads(kwargs["body"])
                self.assertEqual(payload["head_sha"], self.sha)
                self.assertEqual(payload["conclusion"], conclusion)
                self.assertEqual(payload["name"], "Codequiry AI detection")
                self.assertNotIn("fake-test-token", payload["output"]["summary"])

    def test_error_report_uses_trigger_revision_without_analyzing_new_head(self):
        publish({"outcome": "INCONCLUSIVE"}, "fake-test-token", head_sha=self.sha)
        self.assertEqual(json.loads(self.connection.request.call_args.kwargs["body"])["head_sha"], self.sha)

    def test_invalid_or_mismatched_revision_cannot_publish(self):
        for revision in ("new", "b" * 40, None):
            with self.subTest(revision=revision), self.assertRaises(CheckPublishError):
                publish({"head_sha": revision}, "fake-test-token", head_sha=self.sha)
        self.https.assert_not_called()

    def test_publication_failure_is_sanitized_and_not_retried(self):
        self.connection.getresponse.return_value.status = 403
        with self.assertRaisesRegex(CheckPublishError, "HTTP 403"):
            publish({"head_sha": self.sha}, "fake-test-token")
        self.connection.request.assert_called_once()
        self.connection.close.assert_called_once()

    def test_network_failure_does_not_repeat_mutation(self):
        self.connection.request.side_effect = TimeoutError("fake-test-token")
        with self.assertRaises(CheckPublishError) as caught:
            publish({"head_sha": self.sha}, "fake-test-token")
        self.assertNotIn("fake-test-token", str(caught.exception))
        self.connection.request.assert_called_once()
