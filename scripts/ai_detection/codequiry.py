"""Explicit Codequiry REST operations for an authorized service evaluation.

Reference: https://codequiry.com/usage/api/comprehensive
Creating, uploading and starting checks require account authorization. Callers
retain returned check IDs to reconcile interrupted requests before retrying.
"""

from decimal import Decimal
from http.client import HTTPException, HTTPSConnection
from io import BytesIO
import json
import math
import secrets
from zipfile import BadZipFile, ZipFile

from .detector import DetectorUnavailable


_MAX_ZIP_BYTES = 10_000_000
_MAX_RESPONSE_BYTES = 8 * 1024 * 1024


class CodequiryError(DetectorUnavailable):
    """Sanitized request failure. A mutation may have completed remotely."""

    def __init__(self, message: str, *, status: int | None = None, retry_after: int = 30):
        super().__init__(message)
        self.status = status
        self.retry_after = retry_after


def _positive_integer(value: int) -> None:
    if type(value) is not int or value <= 0:
        raise ValueError("Expected a positive integer ID")


class CodequiryClient:
    """Small synchronous client. Construction makes no network requests."""

    def __init__(self, api_key: str, *, timeout: float = 10):
        if not api_key or any(character.isspace() for character in api_key):
            raise ValueError("Provide a nonempty API key without whitespace")
        if not math.isfinite(timeout) or not 0 < timeout <= 60:
            raise ValueError("Timeout must be between 0 and 60 seconds")
        self._api_key = api_key
        self._timeout = timeout

    def _request(
        self, method: str, endpoint: str, *, payload: dict | None = None,
        body: bytes | None = None, content_type: str = "application/json",
    ) -> dict | list:
        headers = {"apikey": self._api_key, "Accept": "application/json",
                   "User-Agent": "Crypto-Trader-Codequiry"}
        if payload is not None:
            body = json.dumps(payload).encode("utf-8")
        if body is not None:
            headers["Content-Type"] = content_type
        connection = HTTPSConnection("codequiry.com", timeout=self._timeout)
        try:
            connection.request(method, "/api/v1/" + endpoint, body=body, headers=headers)
            response = connection.getresponse()
            if not 200 <= response.status < 300:
                # No response body, source, credential, redirect URL or account
                # identity is exposed in the exception.
                wait = response.getheader("Retry-After", "")
                delay = int(wait) if isinstance(wait, str) and wait.isascii() and wait.isdigit() and len(wait) <= 8 else 30
                raise CodequiryError(f"Codequiry returned HTTP {response.status}.",
                                    status=response.status, retry_after=delay)
            raw = response.read(_MAX_RESPONSE_BYTES + 1)
            if len(raw) > _MAX_RESPONSE_BYTES:
                raise CodequiryError("Codequiry response exceeded the size limit.")
            result = json.loads(raw, parse_float=Decimal)
            if not isinstance(result, (dict, list)):
                raise CodequiryError("Codequiry returned an unexpected JSON shape.")
            if isinstance(result, dict) and result.get("success") is False:
                raise CodequiryError("Codequiry reported an unsuccessful operation.")
            return result
        except (HTTPException, OSError):
            raise CodequiryError("Codequiry connection failed or timed out.") from None
        except (ValueError, RecursionError):
            raise CodequiryError("Codequiry returned invalid JSON.") from None
        finally:
            connection.close()

    def account(self) -> dict | list:
        return self._request("GET", "account")

    def languages(self) -> dict | list:
        return self._request("GET", "languages")

    def test_types(self) -> dict | list:
        return self._request("GET", "test-types")

    def create_check(self, name: str, *, language: int, test_type: int) -> int:
        """Select language and engine explicitly, with AI analysis enabled."""
        if not 3 <= len(name) <= 255:
            raise ValueError("Check name must contain 3–255 characters")
        _positive_integer(language)
        _positive_integer(test_type)
        result = self._request("POST", "check/create", payload={
            "name": name, "language": language, "test_type": test_type, "ai_run": True,
        })
        check_id = result.get("id") if isinstance(result, dict) else None
        if type(check_id) is not int or check_id <= 0:
            raise CodequiryError("Check creation returned no valid ID; reconcile before retrying.")
        return check_id

    def upload(self, check_id: int, archive: bytes) -> dict | list:
        _positive_integer(check_id)
        if not archive or len(archive) >= _MAX_ZIP_BYTES:
            raise ValueError("Provide a ZIP smaller than 10 MB")
        try:
            with ZipFile(BytesIO(archive)) as zip_file:
                if not zip_file.infolist() or sum(f.file_size for f in zip_file.infolist()) >= _MAX_ZIP_BYTES:
                    raise ValueError("ZIP must have contents smaller than 10 MB uncompressed")
        except BadZipFile:
            raise ValueError("Provide a valid ZIP archive") from None
        boundary = secrets.token_hex(24)
        body = (
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"check_id\"\r\n\r\n"
            f"{check_id}\r\n--{boundary}\r\n"
            'Content-Disposition: form-data; name="file"; filename="submission.zip"\r\n'
            "Content-Type: application/zip\r\n\r\n"
        ).encode() + archive + f"\r\n--{boundary}--\r\n".encode()
        return self._request("POST", "check/upload", body=body,
                             content_type=f"multipart/form-data; boundary={boundary}")

    def start(self, check_id: int, *, test_type: int) -> dict | list:
        """May consume analysis credits. There are no automatic mutation retries."""
        _positive_integer(check_id)
        _positive_integer(test_type)
        return self._request("POST", "check/start", payload={"check_id": check_id, "test_type": test_type})

    def get_check(self, check_id: int) -> dict | list:
        """Inspect check.status_id before fetching AI results."""
        _positive_integer(check_id)
        return self._request("POST", "check/get", payload={"check_id": check_id})

    def ai_results(self, check_id: int) -> dict | list:
        _positive_integer(check_id)
        return self._request("POST", "ai-results", payload={"assignment_id": check_id})

    def delete_check(self, check_id: int) -> dict | list:
        _positive_integer(check_id)
        return self._request("DELETE", f"checks/{check_id}")
