# training_model.py
from typing import TYPE_CHECKING

from attr import attr
from attrs import define
if TYPE_CHECKING:
    from src.crypto_trader_analysis.apps.learning.models.training.training_model_builder import TrainingModelBuilder
from src.crypto_trader_analysis.apps.learning.models.database.query_type import QueryType


@define
class TrainingModel:
    max_rows: int = attr(default=2000)
    epochs: int = attr(default=35)
    batch_size: int = attr(default=32)
    patience: int = attr(default=5)
    skip_small_samples: bool = attr(default=True)
    sequence_length: int = attr(default=10)
    query_type: QueryType = attr(default=QueryType.HISTORICAL_PRICE)

    def __str__(self) -> str:
        return f"""
        Training Model:
            Max Rows: {self.max_rows}
            Epochs: {self.epochs}
            Batch Size: {self.batch_size}
            Patience: {self.patience}
            Skip Small Samples: {self.skip_small_samples}
            Sequence Length: {self.sequence_length}
            Query Type: {self.query_type}
        """

    @staticmethod
    def builder() -> 'TrainingModelBuilder':
        from src.crypto_trader_analysis.apps.learning.models.training.training_model_builder import TrainingModelBuilder
        return TrainingModelBuilder()