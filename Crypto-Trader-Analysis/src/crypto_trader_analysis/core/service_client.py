from urllib.parse import urljoin

import requests
from django.conf import settings
from requests import Response


def service_url(base_url: str, path: str) -> str:
    return urljoin(f"{base_url.rstrip('/')}/", path.lstrip("/"))


def certificate_verification():
    return settings.CT_CA_BUNDLE or True


def post_json(base_url: str, path: str, payload: dict) -> Response:
    return requests.post(
        service_url(base_url, path),
        json=payload,
        timeout=settings.CT_SERVICE_TIMEOUT_SECONDS,
        verify=certificate_verification(),
    )
