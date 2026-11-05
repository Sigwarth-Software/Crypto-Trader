"""Read Crypto-Trader PR snapshots from GitHub's REST API."""

from http.client import HTTPException, HTTPResponse, HTTPSConnection
import json
import random
import re
import time
from urllib.parse import parse_qs, urlsplit

from .diff import Manifest
from .github_diff import FILE_LIMIT, IncompleteDiff, read_manifest


REPOSITORY = "Sigwarth-Software/Crypto-Trader"
REPOSITORY_ID = 642387518
ORGANIZATION_ID = 279793999
_HOST = "api.github.com"
_PAGE_SIZE = 100
_REQUEST_BYTES = 8 * 1024 * 1024
_TOTAL_BYTES = 32 * 1024 * 1024


class GitHubReadError(Exception):
    """A public diagnostic that never includes response bodies or credentials."""


def _retry_delay(status: int, headers: dict[str, str], attempt: int) -> float | None:
    if status not in {403, 429, 500, 502, 503, 504}:
        return None
    delay = 2 ** attempt + random.random()
    retry_after = headers.get("retry-after")
    if retry_after is not None:
        if not re.fullmatch(r"[0-9]{1,12}", retry_after):
            return None
        delay = max(delay, int(retry_after))
    if headers.get("x-ratelimit-remaining") == "0":
        reset = headers.get("x-ratelimit-reset", "")
        if not re.fullmatch(r"[0-9]{1,12}", reset):
            return None
        return max(delay, int(reset) - time.time())
    if status == 403 and retry_after is None:
        return None
    if status == 429 and retry_after is None:
        return max(60, delay)
    return delay


def _has_next_page(link: str, number: int, next_page: int) -> bool:
    """Validate GitHub's next link. Construct the actual request locally."""
    found = False
    for part in link.split(",") if link else ():
        match = re.fullmatch(r'\s*<([^<>]+)>;\s*rel="([^"]+)"\s*', part)
        if not match:
            raise GitHubReadError("Malformed GitHub pagination header.")
        if match[2] != "next":
            continue
        try:
            url = urlsplit(match[1])
            query = parse_qs(url.query, strict_parsing=True, max_num_fields=2)
        except ValueError as error:
            raise GitHubReadError("Invalid next-page URL.") from error
        paths = {
            f"/repos/{REPOSITORY}/pulls/{number}/files",
            f"/repositories/{REPOSITORY_ID}/pulls/{number}/files",
        }
        if (found or url.scheme != "https" or url.netloc != _HOST or url.path not in paths
                or url.fragment or query != {"per_page": [str(_PAGE_SIZE)], "page": [str(next_page)]}):
            raise GitHubReadError("Unexpected GitHub next-page URL.")
        found = True
    return found


class PullRequestReader:
    """One bounded read of one PR. The optional token is used only at api.github.com."""

    def __init__(self, number: int, token: str | None = None):
        if type(number) is not int or number <= 0:
            raise ValueError("PR number must be a positive integer.")
        self.number = number
        self.path = f"/repos/{REPOSITORY}/pulls/{number}"
        self.headers = {
            "Accept": "application/vnd.github+json",
            "User-Agent": "Crypto-Trader-PR-inspector",
            "X-GitHub-Api-Version": "2026-03-10",
        }
        if token:
            self.headers["Authorization"] = f"Bearer {token}"
        self.deadline = time.monotonic() + 45
        self.bytes_left = _TOTAL_BYTES

    def _get(self, path: str) -> tuple[object, str]:
        for attempt in range(3):
            remaining = self.deadline - time.monotonic()
            if remaining <= 0:
                raise GitHubReadError("GitHub read deadline exceeded.")
            connection = HTTPSConnection(_HOST, timeout=min(10, remaining))
            try:
                connection.request("GET", path, headers=self.headers)
                response = connection.getresponse()
                if response.status == 200:
                    return self._read_json(response), response.getheader("Link", "")
                status = response.status
                headers = {key.lower(): value for key, value in response.getheaders()}
                delay = _retry_delay(status, headers, attempt)
                reason = f"GitHub returned HTTP {status}."
                if status == 429 or (status == 403 and delay is not None):
                    reason = f"GitHub rate limit reached (HTTP {status})."
            except (HTTPException, OSError):
                delay = 2 ** attempt + random.random()
                reason = "GitHub connection failed or timed out."
            finally:
                connection.close()
            if delay is None or attempt == 2 or time.monotonic() + delay >= self.deadline:
                raise GitHubReadError(reason)
            time.sleep(delay)
        raise AssertionError("Unreachable retry state")

    def _read_json(self, response: HTTPResponse) -> object:
        limit = min(self.bytes_left, _REQUEST_BYTES)
        expected = response.length
        if expected is not None and expected > limit:
            raise GitHubReadError("GitHub response exceeded the read size budget.")
        data = bytearray()
        while True:
            if time.monotonic() >= self.deadline:
                raise GitHubReadError("GitHub read deadline exceeded.")
            chunk = response.read1(min(65536, limit - len(data) + 1))
            if not chunk:
                break
            self.bytes_left -= len(chunk)
            data.extend(chunk)
            if len(data) > limit:
                raise GitHubReadError("GitHub response exceeded the read size budget.")
        if expected is not None and len(data) != expected:
            raise GitHubReadError("Truncated GitHub response.")
        try:
            return json.loads(data)
        except (ValueError, RecursionError) as error:
            raise GitHubReadError("Invalid JSON in the GitHub response.") from error

    def snapshot(self) -> dict:
        pr, _ = self._get(self.path)
        if not isinstance(pr, dict) or pr.get("number") != self.number:
            raise GitHubReadError("GitHub returned unexpected PR metadata.")
        base = pr.get("base")
        repo = base.get("repo") if isinstance(base, dict) else None
        if not isinstance(repo, dict) or repo.get("id") != REPOSITORY_ID:
            raise GitHubReadError("GitHub returned an unexpected base repository.")
        return pr

    def manifest(self, before: dict) -> Manifest:
        count = before.get("changed_files")
        if type(count) is not int or count < 0:
            raise IncompleteDiff("Invalid or missing changed_files count.")
        if count > FILE_LIMIT:
            raise IncompleteDiff("The PR exceeds the GitHub files API limit.")
        pages = []
        for page_number in range(1, FILE_LIMIT // _PAGE_SIZE + 1):
            page, link = self._get(f"{self.path}/files?per_page={_PAGE_SIZE}&page={page_number}")
            if not isinstance(page, list) or len(page) > _PAGE_SIZE:
                raise IncompleteDiff("Invalid GitHub files page.")
            pages.append(page)
            if not _has_next_page(link, self.number, page_number + 1):
                return read_manifest(before, pages, self.snapshot())
        raise IncompleteDiff("GitHub pagination exceeded the files API limit.")
