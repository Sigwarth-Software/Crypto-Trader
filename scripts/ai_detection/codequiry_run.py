"""Authorized manual or automatic Codequiry PR evaluation; outputs a report."""

import argparse
from decimal import Decimal, InvalidOperation
import json
import os
from pathlib import Path

from .__main__ import inspect
from .codequiry import CodequiryClient
from .codequiry_detector import CodequiryDetector
from .detector import DetectorUnavailable
from .github_api import PullRequestReader, GitHubReadError
from .github_diff import IncompleteDiff
from .policy import ScorePolicy


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pr_number", type=int)
    parser.add_argument("--engine-id", type=int, required=True)
    parser.add_argument("--receipt", type=Path, required=True, help="Private local job receipt; reuse to resume")
    parser.add_argument("--authorize-upload-and-charges", action="store_true")
    parser.add_argument("--delete-after", action="store_true", help="Request provider deletion after valid results")
    parser.add_argument("--summary", type=Path, help="Write a Markdown report to this local path")
    parser.add_argument("--report", type=Path, help="Write the JSON report for check publishing")
    parser.add_argument("--expected-head-sha", help="Skip obsolete queued PR events before paid analysis")
    args = parser.parse_args(argv)
    if not args.authorize_upload_and_charges:
        parser.error("Explicit upload and charge authorization is required")
    try:
        policy = ScorePolicy(Decimal(os.environ["AI_DETECTION_MAX_SCORE"]),
                             int(os.environ["AI_DETECTION_MIN_LINES"]))
        client = CodequiryClient(os.environ["CODEQUIRY_API_KEY"])
        if args.pr_number <= 0:
            raise ValueError("PR number must be positive")
        detector = CodequiryDetector(client, test_type=args.engine_id, receipt=args.receipt,
                                     revision=f"Crypto-Trader-PR-{args.pr_number}", delete_after=args.delete_after)
    except (KeyError, ValueError, InvalidOperation):
        parser.error("Set CODEQUIRY_API_KEY, a finite 0–100 AI_DETECTION_MAX_SCORE, "
                     "and a positive integer AI_DETECTION_MIN_LINES; use positive PR and engine IDs")
    before = None
    try:
        reader = PullRequestReader(args.pr_number, os.getenv("GITHUB_TOKEN"))
        before = reader.snapshot()
        if (args.expected_head_sha and before["head"]["sha"] != args.expected_head_sha
                or before.get("state", "open") != "open" or before.get("draft", False)):
            raise GitHubReadError("PR is closed, a draft, or no longer matches the triggering revision.")
        detector.revision += ":" + str(before.get("base", {}).get("sha")) + ":" + str(before.get("head", {}).get("sha"))
        report = inspect(reader, before=before,
                         detector=detector, policy=policy)
        if "head_sha" in report:
            latest = PullRequestReader(args.pr_number, os.getenv("GITHUB_TOKEN")).snapshot()
            if (latest["head"]["sha"] != report["head_sha"] or latest["base"]["sha"] != report["base_sha"]
                    or latest["base"]["ref"] != "development"
                    or latest.get("state", "open") != "open" or latest.get("draft", False)):
                report.update(outcome="INCONCLUSIVE", reason="PR changed during analysis.",
                              estimated_ai_likelihood=None, exact_score=None)
    except (GitHubReadError, IncompleteDiff, DetectorUnavailable) as error:
        report = {"outcome": "INCONCLUSIVE", "reason": str(error)}
    report.update(pr_number=args.pr_number, mode="experimental",
                  configured_maximum=str(policy.maximum), minimum_lines=policy.minimum_lines,
                  provider_check_id=detector.check_id, cleanup=detector.cleanup)
    if before:
        report.setdefault("head_sha", before["head"]["sha"])
    # Publish obsolete/error reports on the triggering commit, never a newer head.
    if args.expected_head_sha:
        report["head_sha"] = args.expected_head_sha
    print(json.dumps(report, indent=2))
    if args.report:
        args.report.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    if args.summary:
        args.summary.write_text("# Experimental Codequiry evaluation\n\n~~~json\n"
                                + json.dumps(report, indent=2) + "\n~~~\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
