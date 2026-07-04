import logging
from datetime import datetime
from os import getenv
from typing import Any
from django.conf import settings

import numpy as np
import pandas as pd
from attr import attr
from attrs import define, Factory
from pandas import DataFrame
import tensorflow as tf
from sklearn.preprocessing import MinMaxScaler

from src.crypto_trader_analysis.apps.learning.models.ai.lstm.base_model import BaseModel
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.complex_lstm_model import ComplexLstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.layered.complex_multi_layer_lstm_model import \
    ComplexMultiLayerLstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.layered.multi_layer_lstm_model import \
    MultiLayerLstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.lstm_model import LstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.model_retriever import get_model
from src.crypto_trader_analysis.apps.learning.models.ai.model_type import ModelType
from src.crypto_trader_analysis.apps.learning.models.data.preprocessor import Preprocessor
from src.crypto_trader_analysis.apps.learning.models.database.database import Database
from src.crypto_trader_analysis.apps.learning.models.database.query_load import QueryLoad
from src.crypto_trader_analysis.apps.learning.models.prediction.predictions import actual_vs_predicted, \
    send_prediction_to_server
from src.crypto_trader_analysis.apps.learning.models.training.multi_layer_training_model import \
    MultiLayerTrainingModel
from src.crypto_trader_analysis.apps.learning.models.training.training_model import TrainingModel
from src.crypto_trader_analysis.core.http_client import post_json


@define
class TrainingSession:
    gpu_id: int = attr(default=0)
    training_model: TrainingModel | MultiLayerTrainingModel = attr(default=None)
    prediction_training_model: TrainingModel | MultiLayerTrainingModel = attr(default=None)
    target_currency: str = attr(default='BTC')
    model_type: ModelType = attr(default=ModelType.LSTM)

    _model: BaseModel = attr(default=None)
    _database: Database = attr(factory=Database)

    _training_start_time: datetime = attr(default=None)
    _training_end_time: datetime = attr(default=None)
    _query_start_time: datetime = attr(default=None)
    _query_end_time: datetime = attr(default=None)
    _prediction_id: int = attr(default=0)
    _actual_rows: int = attr(default=0)
    _starting_loss: float = attr(default=0.0)
    _final_loss: float = attr(default=0.0)
    _epochs_trained: int = attr(default=0)
    _dimension_width: int = attr(default=1)
    _query_load: QueryLoad = attr(default=QueryLoad.BULK)
    _query_batch_size: int | None = attr(default=None)

    _short_seq_len: int = attr(default=10)
    _medium_seq_len: int = attr(default=50)
    _long_seq_len: int = attr(default=150)

    def __attrs_post_init__(self):
        if self.prediction_training_model is None:
            self.prediction_training_model = self.training_model
        if isinstance(self.training_model, MultiLayerTrainingModel):
            self._short_seq_len = self.training_model.short_seq_length
            self._medium_seq_len = self.training_model.medium_seq_length
            self._long_seq_len = self.training_model.long_seq_length

    def _get_dataframe(self) -> pd.DataFrame:
        logging.info("Getting dataframe...")
        self._query_start_time = datetime.now()
        dataframe: pd.DataFrame = self._database.fetch_data(self.target_currency,
                                                            self.training_model.max_rows,
                                                            self.training_model.query_type)
        self._query_end_time = datetime.now()
        if dataframe is None or dataframe.empty:
            raise ValueError(f"No data returned from database for {self.target_currency}")
        target_col = f"{self.target_currency.lower()}_price"
        if target_col not in dataframe.columns:
            raise ValueError(
                f"Column '{target_col}' not found in DataFrame. "
                f"Available columns: {list(dataframe.columns)[:5]}..."
            )
        dataframe.dropna(subset=[target_col], inplace=True)
        self._actual_rows = len(dataframe)
        return dataframe

    def _get_batched_dataframe(self) -> list[pd.DataFrame]:
        logging.info("Getting batched dataframe...")
        self._query_load = QueryLoad.BATCHES
        batch_size: int = self.training_model.max_rows // 10
        if self.is_complex_model():
            batch_size = min(batch_size, 15_000)
        self._query_batch_size = batch_size
        self._query_start_time = datetime.now()
        batches: list[pd.DataFrame] = self._database.fetch_data_in_batches(batch_size,
                                                                           self.target_currency,
                                                                           self.training_model.max_rows,
                                                                           self.training_model.query_type)
        self._query_end_time = datetime.now()
        batches = [batch.dropna(subset=[f"{self.target_currency.lower()}_price"]) for batch in batches]
        self._actual_rows = sum(len(batch) for batch in batches)
        return batches

    def is_complex_model(self) -> bool:
        return isinstance(self._model, ComplexLstmModel) \
            or isinstance(self._model, ComplexMultiLayerLstmModel)

    def train(self,
              dataframe: pd.DataFrame | None = None,
              in_batches: bool = False,
              use_previous_model: bool = True) -> None:
        if self.is_multi_layer():
            self._train_multi_layer(dataframe, in_batches, use_previous_model)
            return
        if in_batches or self.is_complex_model():
            self._train_in_batches()
            return
        logging.info(f"Training model for {self.target_currency}...")
        if dataframe is None:
            dataframe: pd.DataFrame = self._get_dataframe()
        if dataframe.empty:
            logging.warning(f"No data available for {self.target_currency}. Skipping training.")
            return
        dataset: tf.data.Dataset
        val_dataset: tf.data.Dataset
        target_scaler: MinMaxScaler
        dataset, val_dataset, target_scaler, historical_prices = self._transform_to_dataset(dataframe)
        self._model = self._get_model(historical_prices, use_previous_model)
        self._training_start_time = datetime.now()
        with tf.device(f"/GPU:{self.gpu_id}"):
            logging.info(f"Training on GPU...")
            history = self._model.train(dataset,
                                        val_dataset=val_dataset,
                                        epochs=self.training_model.epochs,
                                        patience=self.training_model.patience)
        self._training_end_time = datetime.now()
        logging.info(f"Training completed for {self.target_currency}.")
        self._save_model()
        self._capture_prediction()
        self._capture_history_data(history)
        self._send_to_server()

    def _capture_prediction(self):
        try:
            prediction = actual_vs_predicted(target_currency=self.target_currency,
                                             training_model=self.prediction_training_model,
                                             model_type=self.model_type,
                                             start_time=self._training_start_time)
            if prediction is None:
                logging.warning(f"Could not capture prediction for {self.target_currency}")
                return
            prediction_id: int | None = send_prediction_to_server(prediction)
            if prediction_id is not None:
                self._prediction_id = prediction_id
        except Exception as e:
            logging.error(f"Post-training prediction failed for {self.target_currency}: {e}")
            # Don't re-raise — training already completed successfully

    def is_multi_layer(self):
        return self.model_type == ModelType.MULTI_LAYER or self.model_type == ModelType.COMPLEX_MULTI_LAYER

    def _train_multi_layer(self,
                           dataframe: pd.DataFrame | None = None,
                           in_batches: bool = False,
                           use_previous_model: bool = True):
        if in_batches or self.is_complex_model():
            self._train_multi_layer_in_batches()
            return
        logging.info(f"Training multi-layer model for {self.target_currency}...")
        if dataframe is None:
            dataframe: pd.DataFrame = self._get_dataframe()
        if dataframe.empty:
            logging.warning(f"No data available for {self.target_currency}. Skipping training.")
            return
        dataset: tf.data.Dataset
        val_dataset: tf.data.Dataset
        target_scaler: MinMaxScaler
        dataset, val_dataset, target_scaler, short_data, med_data, long_data = self._transform_multi_layer_to_dataset(dataframe)
        self._model = self._get_model(short_data, use_previous_model)
        self._training_start_time = datetime.now()
        with tf.device(f"/GPU:{self.gpu_id}"):
            logging.info(f"Training on GPU...")
            history = self._model.train(dataset,
                                        val_dataset=val_dataset,
                                        epochs=self.training_model.epochs,
                                        patience=self.training_model.patience)
        self._training_end_time = datetime.now()
        logging.info(f"Training completed for {self.target_currency}.")
        self._save_model()
        self._capture_prediction()
        self._capture_history_data(history)
        self._send_to_server()

    def _train_multi_layer_in_batches(self):
        batched_dataframes: list[pd.DataFrame] = self._get_batched_dataframe()
        for dataframe in batched_dataframes:
            self._train_multi_layer(dataframe, in_batches=False)

    def _capture_history_data(self, history):
        losses = history.history.get('loss', [])
        self._epochs_trained = len(losses)
        self._starting_loss = losses[0]
        self._final_loss = losses[-1]

    def predict(self, last_sequence: Any, target_scaler: MinMaxScaler) -> float:
        if self.is_multi_layer():
            return self._predict_multi_layer(last_sequence, target_scaler)
        predicted_price: float = self._model.predict(last_sequence, target_scaler)
        return predicted_price

    def predict_without_dataframe(self) -> float:
        if self.is_multi_layer():
            return self.predict_multilayer_without_dataframe()
        dataframe: pd.DataFrame = self._get_dataframe()
        dataset, val_dataset, target_scaler, historical_prices = self._transform_to_dataset(dataframe)
        self._model = self._get_model(historical_prices, True)
        last_sequence = np.array([historical_prices[-1]])
        predicted_price: float = self._model.predict(last_sequence, target_scaler)
        return predicted_price


    def predict_multilayer_without_dataframe(self) -> float:
        dataframe: pd.DataFrame = self._get_dataframe()
        dataset, val_dataset, target_scaler, short_data, med_data, long_data = self._transform_multi_layer_to_dataset(dataframe)
        self._model = self._get_model(short_data, True)
        predicted_price: float = self._model.predict(self.get_last_multi_layer_elements(long_data, med_data, short_data), target_scaler)
        return predicted_price


    def _predict_multi_layer(self, input_data_list: tuple[Any, Any, Any], target_scaler: MinMaxScaler) -> float:
        predicted_price: float = self._model.predict(input_data_list, target_scaler)
        return predicted_price

    def _save_model(self) -> None:
        logging.info(f"Saving model for {self.target_currency}...")
        model_path: str = self._model.get_model_path(self.target_currency)
        self._model.save_model(model_path)

    def _train_in_batches(self) -> None:
        batched_dataframes: list[pd.DataFrame] = self._get_batched_dataframe()
        for dataframe in batched_dataframes:
            self.train(dataframe, in_batches=False)

    def _transform_to_dataset(self, dataframe: pd.DataFrame) -> tuple[tf.data.Dataset, tf.data.Dataset, MinMaxScaler, Any]:
        logging.info("Transforming data...")
        preprocessor: Preprocessor = Preprocessor(sequence_length=self.training_model.sequence_length)
        historical_prices, future_prices_unscaled, input_scaler = (
            preprocessor.transform(dataframe, self.target_currency))
        self._dimension_width = historical_prices.shape[-1]
        logging.info("Scaling target values...")
        target_scaler: MinMaxScaler = MinMaxScaler(feature_range=(0, 1))
        logging.info("Fitting target scaler on actual y labels...")
        target_scaler.fit(self._rescale_future_prices(future_prices_unscaled))
        logging.info("Scaling future prices...")
        future_prices_scaled = target_scaler.transform(
            self._rescale_future_prices(future_prices_unscaled)).ravel()
        train_ds, val_ds = self._scaled_as_dataset(historical_prices, future_prices_scaled)
        shape_str = f"Preprocessing Completed! historical_prices shape: {historical_prices.shape}, " \
                 f"future_prices shape: {future_prices_unscaled.shape}"
        logging.info(shape_str)
        return train_ds, val_ds, target_scaler, historical_prices

    def _scaled_as_dataset(self, historical_prices, future_prices_scaled) -> tuple[tf.data.Dataset, tf.data.Dataset]:
        logging.info("Creating TensorFlow dataset...")
        total = len(future_prices_scaled)
        val_size = max(1, int(total * 0.1))
        train_size = total - val_size

        train_ds: tf.data.Dataset = tf.data.Dataset.from_tensor_slices(
            (historical_prices[:train_size], future_prices_scaled[:train_size]))
        train_ds = train_ds.shuffle(train_size) \
                           .batch(self.training_model.batch_size) \
                           .prefetch(tf.data.AUTOTUNE)

        val_ds: tf.data.Dataset = tf.data.Dataset.from_tensor_slices(
            (historical_prices[train_size:], future_prices_scaled[train_size:]))
        val_ds = val_ds.batch(self.training_model.batch_size) \
                       .prefetch(tf.data.AUTOTUNE)
        return train_ds, val_ds

    def _transform_multi_layer_to_dataset(self, dataframe: pd.DataFrame) -> tuple[tf.data.Dataset, tf.data.Dataset, MinMaxScaler, Any, Any, Any]:
        logging.info("Transforming multi-layer data...")
        preprocessor: Preprocessor = Preprocessor(sequence_length=self.training_model.sequence_length)
        short_data, med_data, long_data, future_prices_unscaled, input_scaler = (
            preprocessor.transform_multi_scale_with_weights(dataframe, self.target_currency))
        self._dimension_width = short_data.shape[-1]
        logging.info("Scaling target values...")
        target_scaler: MinMaxScaler = MinMaxScaler(feature_range=(0, 1))
        logging.info("Fitting target scaler on actual y labels...")
        target_scaler.fit(self._rescale_future_prices(future_prices_unscaled))
        logging.info("Scaling future prices...")
        future_prices_scaled = target_scaler.transform(
            self._rescale_future_prices(future_prices_unscaled)).ravel()
        train_ds, val_ds = self._scaled_multi_layer_as_dataset(short_data, med_data, long_data, future_prices_scaled)
        shape_str = f"Preprocessing Completed! short_data shape: {short_data.shape}, " \
                 f"med_data shape: {med_data.shape}, long_data shape: {long_data.shape}, " \
                 f"future_prices shape: {future_prices_unscaled.shape}"
        logging.info(shape_str)
        return train_ds, val_ds, target_scaler, short_data, med_data, long_data

    def _scaled_multi_layer_as_dataset(self, short_data, med_data, long_data, future_prices_scaled) -> tuple[tf.data.Dataset, tf.data.Dataset]:
        total = len(future_prices_scaled)
        val_size = max(1, int(total * 0.1))
        train_size = total - val_size

        train_ds: tf.data.Dataset = tf.data.Dataset.from_tensor_slices((
            (short_data[:train_size], med_data[:train_size], long_data[:train_size]),
            future_prices_scaled[:train_size]
        ))
        train_ds = train_ds.shuffle(train_size) \
                           .batch(self.training_model.batch_size) \
                           .prefetch(tf.data.AUTOTUNE)

        val_ds: tf.data.Dataset = tf.data.Dataset.from_tensor_slices((
            (short_data[train_size:], med_data[train_size:], long_data[train_size:]),
            future_prices_scaled[train_size:]
        ))
        val_ds = val_ds.batch(self.training_model.batch_size) \
                       .prefetch(tf.data.AUTOTUNE)
        return train_ds, val_ds

    def _get_model(self, historical_prices: DataFrame, use_previous_model: bool = True):
        logging.info("Getting model...")
        if self.model_type == ModelType.LSTM:
            model_path: str = LstmModel.get_model_path(self.target_currency)
            model = get_model(LstmModel,
                              self.target_currency,
                              model_path,
                              historical_prices,
                              self.training_model,
                              use_previous_model)
        elif self.model_type == ModelType.COMPLEX_LSTM:
            model_path: str = ComplexLstmModel.get_model_path(self.target_currency)
            model = get_model(ComplexLstmModel,
                              self.target_currency,
                              model_path,
                              historical_prices,
                              self.training_model,
                              use_previous_model)
        elif self.model_type == ModelType.MULTI_LAYER:
            model_path: str = MultiLayerLstmModel.get_model_path(self.target_currency)
            model = get_model(MultiLayerLstmModel,
                              self.target_currency,
                              model_path,
                              historical_prices,
                              self.training_model,
                              use_previous_model)
        elif self.model_type == ModelType.COMPLEX_MULTI_LAYER:
            model_path: str = ComplexMultiLayerLstmModel.get_model_path(self.target_currency)
            model = get_model(ComplexMultiLayerLstmModel,
                              self.target_currency,
                              model_path,
                              historical_prices,
                              self.training_model,
                              use_previous_model)
        else:
            raise ValueError(f"Unsupported model type: {self.model_type}")
        return model

    def get_model_type(self) -> ModelType:
        return ModelType.from_instance(self._model)

    def to_json(self) -> dict:
        date_format: str = "%Y-%m-%dT%H:%M:%S"
        json = {
            "currency": self.target_currency,
            "prediction": self._prediction_id,
            "numRows": self._actual_rows,
            "epochsTrained": self._epochs_trained,
            "maxEpochs": self.training_model.epochs,
            "startingLoss": self._starting_loss,
            "finalLoss": self._final_loss,
            "modelType": self.model_type.to_entity_str(),
            "queryType": self.training_model.query_type.value,
            "trainingStartTime": self._training_start_time.strftime(date_format),
            "trainingEndTime": self._training_end_time.strftime(date_format),
            "queryStartTime": self._query_start_time.strftime(date_format),
            "queryEndTime": self._query_end_time.strftime(date_format),
            "sequenceLength": self.training_model.sequence_length,
            "batchSize": self.training_model.batch_size,
            "dimensionWidth": self._dimension_width,
            "queryLoad": self._query_load.value,
            "queryBatchSize": self._query_batch_size,
            "trainingDevice": f"gpu_{self.gpu_id}",
            "shortSequenceLength": self._short_seq_len if self.is_multi_layer() else None,
            "mediumSequenceLength": self._medium_seq_len if self.is_multi_layer() else None,
            "longSequenceLength": self._long_seq_len if self.is_multi_layer() else None
        }
        return json

    def _send_to_server(self):
        payload: dict = self.to_json()
        logging.info("Sending training session to server...")
        try:
            response = post_json(
                settings.CT_DATA_BASE_URL,
                "/data/training-session/add",
                payload,
            )
            if response.status_code != 200:
                logging.error(f"Failed to send training session to server. Status code: {response.status_code}")
                return
            logging.info("Training session sent successfully.")
        except Exception as e:
            logging.error(f"Failed to send training session to server. Error: {e}")


    @staticmethod
    def _get_currency_prices(dataframe: pd.DataFrame, target_currency: str):
        return dataframe[[f"{target_currency.lower()}_price"]].values

    @staticmethod
    def _rescale_future_prices(future_prices_unscaled):
        return future_prices_unscaled.reshape(-1, 1)

    @staticmethod
    def get_last_multi_layer_elements(long_data, med_data, short_data) -> tuple[Any, Any, Any]:
        last_short = np.array([short_data[-1]])
        last_medium = np.array([med_data[-1]])
        last_long = np.array([long_data[-1]])
        return last_short, last_medium, last_long
