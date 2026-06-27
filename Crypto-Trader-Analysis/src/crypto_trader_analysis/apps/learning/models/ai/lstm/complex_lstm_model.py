# complex_lstm_model.py
import logging
from datetime import datetime

from typing_extensions import override
from attrs import define
from keras import Sequential, Input
from keras.layers import Bidirectional, LSTM, Dropout, BatchNormalization, \
    Dense
from keras.saving.save import load_model
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.base_model import BaseModel
import os
from keras.callbacks import EarlyStopping, ModelCheckpoint, ReduceLROnPlateau
import tensorflow as tf

MAX_RETRIES = 2

@define
class ComplexLstmModel(BaseModel):
    def __attrs_post_init__(self):
        self.initialize_model()

    def initialize_model(self):
        self.model = Sequential([
            Input(shape=(self.sequence_length, self.dimension)),
            self.get_lstm_layer(512, True),
            Dropout(0.3),
            BatchNormalization(),
            self.get_lstm_layer(384, True),
            Dropout(0.3),
            BatchNormalization(),
            self.get_lstm_layer(256, True),
            Dropout(0.3),
            BatchNormalization(),
            self.get_lstm_layer(128, False),
            Dropout(0.3),
            Dense(256, activation="relu"),
            Dense(128, activation="relu"),
            Dense(64, activation="relu"),
            Dense(1)
        ])
        self.model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=5e-4),
                           loss="mean_squared_error")
        self.log_model_summary()

    @staticmethod
    def get_lstm_layer(layers: int, return_sequences: bool):
        return Bidirectional(LSTM(layers,
                                  return_sequences=return_sequences,
                                  recurrent_activation="sigmoid",
                                  use_bias=True,
                                  unroll=True))

    @override
    @staticmethod
    def get_tensorboard_callback(target_currency: str):
        import tensorflow as tf
        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        log_dir = os.path.join("logs", "COMPLEX_LSTM_MODEL", target_currency, timestamp)
        return tf.keras.callbacks.TensorBoard(
            log_dir=log_dir,
            profile_batch=10,
            histogram_freq=1
        )

    @override
    def train(self,
              dataset: tf.data.Dataset,
              val_dataset: tf.data.Dataset = None,
              epochs: int = 20,
              patience: int = 5):
        from src.crypto_trader_analysis.apps.learning.models.ai.model_retriever import COMPLEX_MODEL_DIRECTORY
        checkpoint_dir = os.path.join(COMPLEX_MODEL_DIRECTORY, "checkpoints")
        os.makedirs(checkpoint_dir, exist_ok=True)
        checkpoint_path = os.path.join(checkpoint_dir,
                                       f"{self.target_currency}_checkpoint.keras")
        monitor = "val_loss" if val_dataset is not None else "loss"
        callbacks = [
            EarlyStopping(monitor=monitor, patience=patience,
                          restore_best_weights=True),
            ModelCheckpoint(filepath=checkpoint_path, monitor=monitor,
                            save_best_only=True),
            ReduceLROnPlateau(monitor=monitor, factor=0.5, patience=3,
                              min_lr=1e-6),
            ComplexLstmModel.get_tensorboard_callback(self.target_currency)
        ]
        for attempt in range(MAX_RETRIES + 1):
            try:
                return self.model.fit(dataset,
                               epochs=epochs,
                               verbose=1,
                               validation_data=val_dataset,
                               callbacks=callbacks)
            except Exception as exception:
                from src.crypto_trader_analysis.apps.learning.models.ai.model_type import ModelType
                from src.crypto_trader_analysis.apps.learning.models.ai.model_retriever import model_exists, \
                    delete_model
                if attempt < MAX_RETRIES and "Input 0 of layer" in str(exception):
                    logging.warning(f"Model dimension mismatch (attempt {attempt + 1}). Re-initializing model.")
                    complex_lstm_model_exists: bool = model_exists(self.target_currency,
                                                      ModelType.COMPLEX_LSTM)
                    if complex_lstm_model_exists:
                        delete_model(self.target_currency,
                                     ModelType.COMPLEX_LSTM)
                    self.initialize_model()
                else:
                    raise

    @override
    def predict(self, training_data, target_scaler):
        scaled_pred = self.model.predict(training_data)
        real_price = target_scaler.inverse_transform(scaled_pred)[0][0]
        return real_price

    @override
    def save_model(self, path: str):
        self.model.save(path)

    @override
    def load_model(self, path: str):
        self.model = load_model(path)

    @staticmethod
    @override
    def get_model_path(target_currency: str) -> str:
        from src.crypto_trader_analysis.apps.learning.models.ai.model_retriever import COMPLEX_MODEL_DIRECTORY
        return COMPLEX_MODEL_DIRECTORY + target_currency + '_complex_model.keras'
