"""Evaluate a complete manifest with an explicitly supplied detector and policy."""

from collections.abc import Mapping, Sequence
from dataclasses import dataclass
from decimal import Decimal
from enum import StrEnum
from fractions import Fraction

from .detector import Detector, DetectorUnavailable
from .diff import Manifest
from .github_diff import IncompleteDiff, read_manifest


@dataclass(frozen=True)
class ScorePolicy:
    maximum: Decimal
    minimum_lines: int

    def __post_init__(self) -> None:
        if not self.maximum.is_finite() or not 0 <= self.maximum <= 100:
            raise ValueError("maximum must be a finite score between 0 and 100")
        if type(self.minimum_lines) is not int or self.minimum_lines < 1:
            raise ValueError("minimum_lines must be a positive integer")


class Outcome(StrEnum):
    PASS = "PASS"
    FAIL = "FAIL"
    INCONCLUSIVE = "INCONCLUSIVE"
    NO_ELIGIBLE_CODE = "NO_ELIGIBLE_CODE"
    FORK_EXEMPT = "FORK_EXEMPT"


@dataclass(frozen=True)
class Decision:
    outcome: Outcome
    reason: str
    analyzed_lines: int = 0
    score: Fraction | None = None


def evaluate_diff(
    before: Mapping, file_pages: Sequence[list], after: Mapping,
    *, detector: Detector | None = None, policy: ScorePolicy | None = None,
) -> Decision:
    """Validate all saved response pages before making any detector request."""
    try:
        manifest = read_manifest(before, file_pages, after)
    except IncompleteDiff as error:
        return Decision(Outcome.INCONCLUSIVE, str(error))
    return evaluate(manifest, detector=detector, policy=policy)


def evaluate(
    manifest: Manifest | None,
    *,
    detector: Detector | None = None,
    policy: ScorePolicy | None = None,
) -> Decision:
    """A missing manifest means the diff reader could not establish completeness."""
    if manifest is None:
        return Decision(Outcome.INCONCLUSIVE, "Diff completeness could not be verified.")
    eligible = manifest.eligible_lines
    if eligible == 0:
        return Decision(Outcome.NO_ELIGIBLE_CODE, "The complete diff contains no eligible additions.")
    if policy is None or detector is None:
        return Decision(Outcome.INCONCLUSIVE, "A detector and scoring policy are required.")
    if eligible < policy.minimum_lines:
        return Decision(Outcome.INCONCLUSIVE, "Too few eligible added lines for the scoring policy.")

    try:
        scores = detector.score(manifest.components)
    except DetectorUnavailable:
        return Decision(Outcome.INCONCLUSIVE, "The detector could not complete the request.")

    expected_ids = {component.component_id for component in manifest.components}
    if scores.keys() - expected_ids:
        return Decision(Outcome.INCONCLUSIVE, "Detector returned an unexpected component ID.")
    weighted_sum = Fraction(0)
    analyzed = 0
    for component in manifest.components:
        score = scores.get(component.component_id)
        if isinstance(score, Decimal) and score.is_finite() and 0 <= score <= 100:
            weighted_sum += Fraction(score) * component.eligible_lines
            analyzed += component.eligible_lines
    if analyzed != eligible:
        return Decision(Outcome.INCONCLUSIVE, "Missing or invalid component scores.", analyzed)

    score = weighted_sum / analyzed
    if score > policy.maximum:
        return Decision(Outcome.FAIL, "Estimated score exceeds the configured maximum.", analyzed, score)
    return Decision(Outcome.PASS, "Estimated score is within the configured maximum.", analyzed, score)
