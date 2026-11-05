"""Inspect the input to the gate. This command has no check-publishing capability."""

import argparse
import json
import os

from .github_api import GitHubReadError, ORGANIZATION_ID, PullRequestReader
from .github_diff import IncompleteDiff
from .policy import Outcome, evaluate


def inspect(reader: PullRequestReader) -> dict:
    before = reader.snapshot()
    base_ref = before["base"].get("ref")
    if not isinstance(base_ref, str) or not base_ref:
        raise GitHubReadError("Missing PR target branch.")
    if base_ref != "development":
        return {"outcome": "OUT_OF_SCOPE", "reason": "PR targets another branch."}
    head = before.get("head")
    repo = head.get("repo") if isinstance(head, dict) else None
    owner = repo.get("owner") if isinstance(repo, dict) else None
    owner_id = owner.get("id") if isinstance(owner, dict) else None
    if type(owner_id) is not int or owner_id <= 0:
        raise GitHubReadError("Cannot verify the PR head repository owner.")
    if owner_id != ORGANIZATION_ID:
        return {"outcome": Outcome.FORK_EXEMPT, "reason": "Head repository belongs to an external owner."}

    manifest = reader.manifest(before)
    decision = evaluate(manifest)
    return {
        "outcome": decision.outcome,
        "reason": decision.reason,
        "head_sha": head["sha"],
        "physical_added_lines": manifest.physical_lines,
        "eligible_added_lines": manifest.eligible_lines,
        "excluded_added_lines": manifest.physical_lines - manifest.eligible_lines,
        "analyzed_added_lines": decision.analyzed_lines,
    }


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pr_number", type=int, help="Crypto-Trader pull request number")
    parser.add_argument("--authenticated", action="store_true", help="Use GITHUB_TOKEN for read access")
    args = parser.parse_args(argv)
    if args.pr_number <= 0:
        parser.error("PR number must be positive")
    token = os.getenv("GITHUB_TOKEN") if args.authenticated else None
    if args.authenticated and not token:
        parser.error("--authenticated requires GITHUB_TOKEN")
    try:
        report = inspect(PullRequestReader(args.pr_number, token))
    except (GitHubReadError, IncompleteDiff) as error:
        print(json.dumps({"pr_number": args.pr_number, "outcome": Outcome.INCONCLUSIVE, "reason": str(error)}))
        return 1
    print(json.dumps({"pr_number": args.pr_number, **report}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
