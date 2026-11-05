"""Pinned, CPU-only research models; optional dependencies stay outside the CLI."""

from decimal import Decimal
from functools import lru_cache
import hashlib
from pathlib import Path

from ..diff import Component


# Each directory must contain exactly the four reviewed release files.
MODEL_HASHES = {
    "VKR_Model": {
        "config.json": "50e7488c95bf29c920b789ded899ba2ed2a2c9f939a85571b7988b6487e26d72",
        "tokenizer.json": "6143ead457e07ba54af91ee518dadb0468b939c892550b0fe04d939b94c248a8",
        "tokenizer_config.json": "aa6a8a3f069d2d98c09444ee3f9671d64c29ce647473dccd78169b10c490d359",
        "model.safetensors": "da991d429826fe2e45d6c9c26437163ad97bfe8aab05e7135b1b2377b1cd18c6",
    },
    "GraphCodeBERT": {
        "config.json": "1d10dda9aec4657702dddfe00971ebd705fd072f581abb326f4dfb712a8c733b",
        "tokenizer.json": "2bb1a22cfbe25b8e5a232b7fc4d7fc5073923b45724a5f813b00811bb6620f66",
        "tokenizer_config.json": "3e524fcbb3d446afb921a867d6f354e1873d142f4a597aecda076f730361616a",
        "model.safetensors": "babd5890473a83a2cc134eea6510f56a09e9b665511011c2ddbd1e2d9d7bbf66",
    },
}


class LocalClassifier:
    def __init__(self, name: str, directory: Path):
        import torch
        from transformers import AutoTokenizer, RobertaForSequenceClassification

        expected = MODEL_HASHES[name]
        if {p.name for p in directory.iterdir()} != set(expected):
            raise ValueError("Stage only the four pinned release files in the model directory.")
        for filename, digest in expected.items():
            with (directory / filename).open("rb") as source:
                if hashlib.file_digest(source, "sha256").hexdigest() != digest:
                    raise ValueError(f"Model file checksum mismatch: {filename}")
        self.name = name
        torch.set_num_threads(4)
        self.tokenizer = AutoTokenizer.from_pretrained(directory, local_files_only=True, trust_remote_code=False)
        self.model, info = RobertaForSequenceClassification.from_pretrained(
            directory, use_safetensors=True, local_files_only=True, output_loading_info=True,
        )
        if info["missing_keys"] or info["mismatched_keys"] or self.model.config.num_labels != 2:
            raise ValueError("Checkpoint did not initialize the expected two-class model.")
        self.model.eval()

    @lru_cache(maxsize=256)
    def _score_text(self, text: str) -> Decimal | None:
        import torch

        inputs = self.tokenizer(text, return_tensors="pt", truncation=False, verbose=False)
        if inputs["input_ids"].shape[1] > 512:
            return None
        with torch.inference_mode():
            logits = self.model(**inputs).logits
            ai_probability = logits.softmax(dim=-1)[0, 1].item()
        return Decimal(str(ai_probability)) * 100

    def score(self, components: tuple[Component, ...]) -> dict[str, Decimal]:
        results = {}
        for component in components:
            if self.name == "VKR_Model" and component.language != "python":
                continue
            value = self._score_text("\n".join(component.block.lines))
            if value is not None:
                results[component.component_id] = value
        return results
