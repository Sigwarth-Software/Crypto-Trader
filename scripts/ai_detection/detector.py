"""Contract shared by the benchmark runner and a future validated adapter."""

from collections.abc import Mapping
from decimal import Decimal
from typing import Protocol

from .diff import Component


class DetectorUnavailable(Exception):
    """A recoverable classification failure, such as a provider timeout."""


class Detector(Protocol):
    def score(self, components: tuple[Component, ...]) -> Mapping[str, Decimal]:
        """Return normalized 0–100 scores by ID, omitting unavailable results.

        An adapter must reject duplicate response IDs before creating the mapping.
        """
        ...
