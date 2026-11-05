"""Accept added ranges from a diff reader and prepare source for a detector."""

from dataclasses import dataclass
from pathlib import PurePosixPath


LANGUAGES = {
    ".java": "java",
    ".kt": "kotlin",
    ".kts": "kotlin",
    ".py": "python",
    ".ts": "typescript",
    ".tsx": "typescript",
    ".js": "javascript",
    ".jsx": "javascript",
}
EXCLUDED_DIRECTORIES = frozenset({
    ".angular", ".gradle", "__pycache__", "build", "dist", "generated",
    "node_modules", "target", "vendor", "vendored",
})


@dataclass(frozen=True)
class AddedBlock:
    """Contiguous added lines without line endings, numbered in the head file."""

    path: str
    first_line: int
    lines: tuple[str, ...]


@dataclass(frozen=True)
class Component:
    """One detector input. The ID is local to a single evaluation."""

    component_id: str
    language: str
    block: AddedBlock
    eligible_lines: int


@dataclass(frozen=True)
class Manifest:
    physical_lines: int
    components: tuple[Component, ...]

    @property
    def eligible_lines(self) -> int:
        return sum(component.eligible_lines for component in self.components)


def build_manifest(blocks: tuple[AddedBlock, ...]) -> Manifest:
    """The caller supplies text additions from a verified, complete diff."""
    components = []
    physical_lines = 0
    last_end_by_path: dict[str, int] = {}
    for block in sorted(blocks, key=lambda item: (item.path, item.first_line)):
        if not block.lines or block.first_line <= last_end_by_path.get(block.path, 0):
            raise ValueError("Expected nonempty, disjoint, one-based added ranges")
        if any("\n" in line for line in block.lines):
            raise ValueError("Each entry must represent one physical line")
        last_end_by_path[block.path] = block.first_line + len(block.lines) - 1
        physical_lines += len(block.lines)
        path = PurePosixPath(block.path)
        language = LANGUAGES.get(path.suffix.lower())
        if language is None or EXCLUDED_DIRECTORIES.intersection(path.parts[:-1]):
            continue
        eligible_lines = sum(1 for line in block.lines if line.strip())
        if eligible_lines:
            components.append(Component(str(len(components) + 1), language, block, eligible_lines))
    return Manifest(physical_lines, tuple(components))
