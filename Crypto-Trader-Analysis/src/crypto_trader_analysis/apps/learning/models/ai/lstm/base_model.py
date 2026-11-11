import logging
from abc import ABC, abstractmethod

from attr import attr
from attrs import define
from keras import Sequential, Model
from sklearn.preprocessing import MinMaxScaler
import tensorflow as tf


@define
class BaseModel(ABC):
    dimension = attr(default=None)
    model: Model = attr(factory=Sequential)
    sequence_length: int = attr(default=10)
    target_currency: str = attr(default='BTC')

    @abstractmethod
    def train(self,
              dataset: tf.data.Dataset,
              val_dataset: tf.data.Dataset = None,
              epochs: int = 20,
              patience: int = 5):
        pass

    @abstractmethod
    def predict(self,
                training_data,
                scaler: MinMaxScaler):
        pass

    @abstractmethod
    def save_model(self, path: str):
        pass

    @abstractmethod
    def load_model(self, path: str):
        pass

    @staticmethod
    @abstractmethod
    def get_model_path(target_currency: str) -> str:
        pass

    @staticmethod
    def get_tensorboard_callback(target_currency: str):
        pass

    def log_model_summary(self):
        lines: list[str] = []
        self.model.summary(print_fn=lambda line, *args, **kwargs: lines.append(line))
        logging.info("Model Summary:\n" + "\n".join(lines))