from contextlib import redirect_stdout
from copy import deepcopy
from io import BytesIO, StringIO
import json
from pathlib import Path
import unittest
from unittest.mock import Mock, patch

from scripts.ai_detection.__main__ import inspect, main
from scripts.ai_detection.github_api import GitHubReadError, ORGANIZATION_ID, PullRequestReader, REPOSITORY_ID
from scripts.ai_detection.github_diff import IncompleteDiff


def response(payload=None, *, status=200, link="", headers=()):
    reply = Mock(status=status)
    data = json.dumps(payload).encode()
    reply.length = len(data)
    reply.read1.side_effect = BytesIO(data).read
    reply.getheader.return_value = link
    reply.getheaders.return_value = headers
    return reply


class GitHubReaderTests(unittest.TestCase):
    def setUp(self):
        fixture = json.loads((Path(__file__).parent / "fixtures" / "pr_files.json").read_text())
        self.pr = fixture["pull_request"]
        self.pr["base"]["repo"]["id"] = REPOSITORY_ID
        self.pr["head"]["repo"]["owner"] = {"id": ORGANIZATION_ID}
        self.pages = fixture["pages"]
        self.next_url = f"https://api.github.com/repositories/{REPOSITORY_ID}/pulls/42/files?per_page=100&page=2"
        https = self.enterContext(patch("scripts.ai_detection.github_api.HTTPSConnection"))
        self.connection = https.return_value
        self.sleep = self.enterContext(patch("scripts.ai_detection.github_api.time.sleep"))
        # Dummy token for mocked HTTP requests. This fixture has no GitHub credentials.
        self.reader = PullRequestReader(42, "test-only")

    def test_inspects_two_pages_between_snapshots(self):
        self.connection.getresponse.side_effect = [
            response(self.pr), response(self.pages[0], link=f'<{self.next_url}>; rel="next"'),
            response(self.pages[1]), response(self.pr),
        ]
        report = inspect(self.reader)
        self.assertEqual(report["outcome"], "INCONCLUSIVE")
        self.assertEqual(report["physical_added_lines"], 7)
        self.assertEqual(report["eligible_added_lines"], 4)
        self.assertEqual(report["excluded_added_lines"], 3)
        self.assertEqual(report["analyzed_added_lines"], 0)
        self.assertEqual(report["head_sha"], self.pr["head"]["sha"])
        self.assertEqual([call.args for call in self.connection.request.call_args_list], [
            ("GET", self.reader.path), ("GET", self.reader.path + "/files?per_page=100&page=1"),
            ("GET", self.reader.path + "/files?per_page=100&page=2"), ("GET", self.reader.path),
        ])

    def test_rejects_foreign_redirects_and_pagination(self):
        for url in (self.next_url.replace("api.github.com", "example.org"),
                    self.next_url.replace("42/files", "41/files"),
                    self.next_url.replace("page=2", "page=1")):
            with self.subTest(url=url):
                self.connection.reset_mock()
                self.connection.getresponse.side_effect = [
                    response(self.pr), response(self.pages[0], link=f'<{url}>; rel="next"'),
                ]
                with self.assertRaisesRegex(GitHubReadError, "next-page URL"):
                    inspect(self.reader)
                self.assertEqual(self.connection.request.call_count, 2)
        self.connection.reset_mock()
        self.connection.getresponse.side_effect = [response(status=302, headers=[("Location", "https://example.org")])]
        with self.assertRaisesRegex(GitHubReadError, "HTTP 302"):
            inspect(self.reader)
        self.assertEqual(self.connection.request.call_count, 1)

    def test_rate_limit_or_timeout_retries_are_bounded(self):
        self.connection.getresponse.side_effect = [
            response(status=429, headers=[("Retry-After", "3")]), TimeoutError(), response(self.pr),
        ]
        self.assertEqual(self.reader.snapshot(), self.pr)
        self.assertEqual(self.sleep.call_count, 2)
        self.assertGreaterEqual(self.sleep.call_args_list[0].args[0], 3)
        self.connection.getresponse.side_effect = [TimeoutError()] * 3
        with self.assertRaisesRegex(GitHubReadError, "timed out"):
            self.reader.snapshot()
        self.assertEqual(self.connection.request.call_count, 6)

    def test_long_reset_or_permission_denial_stops_immediately(self):
        for headers in ([], [("X-RateLimit-Remaining", "0"), ("X-RateLimit-Reset", "9999999999")]):
            with self.subTest(headers=headers):
                self.connection.getresponse.side_effect = [response(status=403, headers=headers)]
                with self.assertRaisesRegex(GitHubReadError, "HTTP 403"):
                    self.reader.snapshot()
                self.sleep.assert_not_called()

    def test_unverifiable_or_exempt_pr_never_fetches_files(self):
        self.pr["head"]["repo"]["owner"]["id"] = 99
        self.connection.getresponse.return_value = response(self.pr)
        self.assertEqual(inspect(self.reader)["outcome"], "FORK_EXEMPT")
        self.pr["head"]["repo"] = None
        self.connection.getresponse.return_value = response(self.pr)
        with self.assertRaisesRegex(GitHubReadError, "head repository owner"):
            inspect(self.reader)
        self.pr["base"]["ref"] = "main"
        self.connection.getresponse.return_value = response(self.pr)
        self.assertEqual(inspect(self.reader)["outcome"], "OUT_OF_SCOPE")
        self.assertEqual(self.connection.request.call_count, 3)

    def test_missing_page_or_changed_head_invalidates_live_read(self):
        after = deepcopy(self.pr)
        for scenario in ("missing_page", "head_changed"):
            with self.subTest(scenario=scenario):
                page = sum(self.pages, [])
                if scenario == "missing_page":
                    page = self.pages[0]
                else:
                    after["head"]["sha"] = "d" * 40
                self.connection.getresponse.side_effect = [response(self.pr), response(page), response(after)]
                with self.assertRaises(IncompleteDiff):
                    inspect(self.reader)

    def test_cli_reports_safe_fetch_failure(self):
        reply = response("secret example")
        reply.length = None
        reply.read1.side_effect = BytesIO(b"invalid response containing secret example").read
        self.connection.getresponse.return_value = reply
        output = StringIO()
        with redirect_stdout(output):
            self.assertEqual(main(["42"]), 1)
        self.assertEqual(json.loads(output.getvalue())["outcome"], "INCONCLUSIVE")
        self.assertNotIn("secret", output.getvalue())
        self.assertNotIn("Authorization", self.connection.request.call_args.kwargs["headers"])

    def test_oversized_and_truncated_bodies(self):
        for length, reason in ((None, "size budget"), (10_000_000, "size budget"), (4, "Truncated")):
            with self.subTest(length=length):
                self.reader.bytes_left = 5
                reply = response()
                reply.length = length
                reply.read1.side_effect = BytesIO(b"{}" if length == 4 else b"123456").read
                self.connection.getresponse.return_value = reply
                with self.assertRaisesRegex(GitHubReadError, reason):
                    self.reader.snapshot()

    def test_expired_deadline_stops_before_network_access(self):
        self.reader.deadline = 0
        with self.assertRaisesRegex(GitHubReadError, "deadline"):
            self.reader.snapshot()
        self.connection.request.assert_not_called()
