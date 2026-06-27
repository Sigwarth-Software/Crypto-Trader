# multi_layer_lstm_model.py
import logging
from datetime import datetime

from typing_extensions import override

from attrs import define

from keras import Input, Model
from keras.layers import LSTM, Dropout, Dense, Concatenate
from keras.saving.save import load_model
from sklearn.preprocessing import MinMaxScaler

from src.crypto_trader_analysis.apps.learning.models.ai.lstm.layered.multi_layer_base_model import MultiLayerBaseModel
import os
from keras.callbacks import EarlyStopping, ModelCheckpoint, ReduceLROnPlateau

MAX_RETRIES = 2


@define
class MultiLayerLstmModel(MultiLayerBaseModel):
    def __attrs_post_init__(self):
        self.initialize_model()

    def initialize_model(self):
        import tensorflow as tf
        if self.dimension is None:
            raise ValueError(
                "Please specify 'dimension' for MultiLayerLstmModel.")
        short_input = Input(shape=(self.short_seq_len, self.dimension),
                            name="short_input")
        medium_input = Input(shape=(self.medium_seq_len, self.dimension),
                             name="medium_input")
        long_input = Input(shape=(self.long_seq_len, self.dimension),
                           name="long_input")
        short_lstm = self.combine_model(short_input)
        medium_lstm = self.combine_model(medium_input)
        long_lstm = self.combine_model(long_input)

        merged = Concatenate()([short_lstm, medium_lstm, long_lstm])
        merged = Dense(50, activation="relu")(merged)
        final_model = Dense(1)(merged)
        self.model = Model(
            inputs=[short_input, medium_input, long_input],
            outputs=final_model,
            name="MultiLayerLstmModel"
        )
        self.model.compile(optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
                           loss="mean_squared_error")
        self.log_model_summary()


    @staticmethod
    @override
    def combine_model(model_input):
        lstm = LSTM(150, return_sequences=True,
                    recurrent_activation="sigmoid", use_bias=True,
                    unroll=True)(model_input)
        lstm = Dropout(0.2)(lstm)
        lstm = LSTM(100, recurrent_activation="sigmoid", use_bias=True,
                    unroll=True)(lstm)
        lstm = Dense(50, activation="relu")(lstm)
        return lstm


    @override
    @staticmethod
    def get_tensorboard_callback(target_currency: str):
        import tensorflow as tf
        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        log_dir = os.path.join("logs", "MULTI_LAYER_LSTM", target_currency, timestamp)

        return tf.keras.callbacks.TensorBoard(
            log_dir=log_dir,
            profile_batch=10,
            histogram_freq=1
        )

    def train(self, dataset,
              val_dataset=None,
              epochs=20, patience=5):
        from src.crypto_trader_analysis.apps.learning.models.ai.model_retriever import MULTI_LAYER_MODEL_DIRECTORY
        checkpoint_dir = os.path.join(MULTI_LAYER_MODEL_DIRECTORY,
                                      "checkpoints")
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
            MultiLayerLstmModel.get_tensorboard_callback(self.target_currency)
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
                    multi_model_exists: bool = model_exists(self.target_currency,
                                                      ModelType.MULTI_LAYER)
                    if multi_model_exists:
                        delete_model(self.target_currency,
                                     ModelType.MULTI_LAYER)
                    self.initialize_model()
                else:
                    raise

    def predict(self, input_data_list, target_scaler: MinMaxScaler):
        pred_scaled = self.model.predict(input_data_list)
        real_price = target_scaler.inverse_transform(pred_scaled)[0][0]
        return real_price

    def save_model(self, path: str):
        self.model.save(path)

    def load_model(self, path: str):
        self.model = load_model(path)

    @staticmethod
    @override
    def get_model_path(target_currency: str) -> str:
        from src.crypto_trader_analysis.apps.learning.models.ai.model_retriever import MULTI_LAYER_MODEL_DIRECTORY
        return MULTI_LAYER_MODEL_DIRECTORY + target_currency + '_multi_layer_model.keras'
