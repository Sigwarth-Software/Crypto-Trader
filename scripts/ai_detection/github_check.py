"""Publish a completed Codequiry check on the analyzed PR head, without retries."""

import argparse
from http.client import HTTPException, HTTPSConnection
import json
import os
from pathlib import Path
import re

from .github_api import REPOSITORY


class CheckPublishError(Exception):
    """Safe public diagnostic excluding tokens and response bodies."""


def publish(report: dict, token: str, *, head_sha: str = "", details_url: str = "") -> None:
    revision = report.get("head_sha", head_sha)
    if (not token or not isinstance(revision, str) or not re.fullmatch(r"[0-9a-f]{40}", revision)
            or head_sha and revision != head_sha):
        raise CheckPublishError("A token and matching analyzed head SHA are required.")
    outcome = report.get("outcome")
    conclusion = {"PASS": "success", "FAIL": "failure", "NO_ELIGIBLE_CODE": "success"}.get(outcome, "neutral")
    payload = {
        "name": "Codequiry AI detection",
        "head_sha": revision,
        "status": "completed",
        "conclusion": conclusion,
        "output": {
            "title": f"Codequiry: {outcome}",
            "summary": "Experimental estimate; human review is required.\n\n~~~json\n"
                       + json.dumps(report, indent=2) + "\n~~~",
        },
    }
    if details_url:
        if not re.fullmatch(r"https://github\.com/" + re.escape(REPOSITORY) + r"/actions/runs/[0-9]+", details_url):
            raise CheckPublishError("Unexpected workflow run URL.")
        payload["details_url"] = details_url
    connection = HTTPSConnection("api.github.com", timeout=20)
    try:
        connection.request("POST", f"/repos/{REPOSITORY}/check-runs",
                           body=json.dumps(payload).encode(), headers={
                               "Authorization": f"Bearer {token}",
                               "Accept": "application/vnd.github+json",
                               "Content-Type": "application/json",
                               "User-Agent": "Crypto-Trader-Codequiry",
                               "X-GitHub-Api-Version": "2022-11-28",
                           })
        response = connection.getresponse()
        if response.status != 201:
            raise CheckPublishError(f"GitHub check publication returned HTTP {response.status}.")
    except (OSError, HTTPException) as error:
        raise CheckPublishError("GitHub check publication failed; inspect the run before retrying.") from error
    finally:
        connection.close()


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path)
    parser.add_argument("--head-sha", default="")
    parser.add_argument("--details-url", default="")
    args = parser.parse_args(argv)
    try:
        publish(json.loads(args.report.read_text(encoding="utf-8")), os.getenv("GITHUB_TOKEN", ""),
                head_sha=args.head_sha, details_url=args.details_url)
    except (CheckPublishError, OSError, ValueError) as error:
        parser.exit(1, f"{error}\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
