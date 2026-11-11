# model_retriever.py
import logging
import os
from pathlib import Path
from typing import TypeVar

from src.crypto_trader_analysis.apps.learning.models.ai.lstm.complex_lstm_model import ComplexLstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.layered.complex_multi_layer_lstm_model import \
    ComplexMultiLayerLstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.lstm_model import LstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.lstm.layered.multi_layer_lstm_model import MultiLayerLstmModel
from src.crypto_trader_analysis.apps.learning.models.ai.model_type import ModelType
from src.crypto_trader_analysis.apps.learning.models.training.training_model import TrainingModel

BASE_DIRECTORY = 'models/'

LSTM_MODEL_DIRECTORY = BASE_DIRECTORY + 'lstm_models/'
COMPLEX_MODEL_DIRECTORY = BASE_DIRECTORY + 'complex_lstm_models/'
MULTI_LAYER_MODEL_DIRECTORY = BASE_DIRECTORY + 'multi_layer_models/'
COMPLEX_MULTI_LAYER_MODEL_DIRECTORY = BASE_DIRECTORY + 'complex_multi_layer_models/'

T = TypeVar('T')

def get_model(model_type: T,
              model_target_currency: str,
              currency_model_path: str,
              historical_prices,
              training_model: TrainingModel,
              use_previous_model: bool = True) -> T:
    if os.path.exists(currency_model_path) and use_previous_model:
        logging.info(f"Found existing model: {currency_model_path}. Loading and continuing training...")
        model = model_type(dimension=historical_prices.shape[2],
                           target_currency=model_target_currency,
                           sequence_length=training_model.sequence_length)
        model.load_model(currency_model_path)
        return model
    else:
        logging.info(f"No existing model found. Creating a new model for {model_target_currency}...")
        return model_type(dimension=historical_prices.shape[2],
                          target_currency=model_target_currency,
                          sequence_length=training_model.sequence_length)


def model_exists(target_currency: str, model_type: ModelType = ModelType.LSTM) -> bool:
    model_file: str = model_type.value.get_model_path(target_currency)
    current_file_path = Path(__file__).resolve()
    base_dir = current_file_path.parents[3]
    model_path = base_dir / model_file
    exists = model_path.is_file()
    logging.debug(f"Checking model at: {model_path} -> Exists: {exists}")
    return exists

def delete_model(target_currency: str, model_type: ModelType = ModelType.LSTM) -> None:
    model_file: str = model_type.value.get_model_path(target_currency)
    current_file_path = Path(__file__).resolve()
    base_dir = current_file_path.parents[3]
    model_path = base_dir / model_file
    if model_path.is_file():
        logging.debug(f"Deleting model at: {model_path}")
        model_path.unlink()
    else:
        logging.debug(f"Model file not found at: {model_path}, nothing to delete.")

def delete_checkpoint(target_currency: str, model_type: ModelType = ModelType.LSTM) -> None:
    checkpoint_file: str = model_type.value.get_model_path(target_currency)
    current_file_path = Path(__file__).resolve()
    base_dir = current_file_path.parents[3]
    checkpoint_path = base_dir / "checkpoints" / checkpoint_file
    if checkpoint_path.is_file():
        logging.info(f"Deleting checkpoint at: {checkpoint_path}")
        checkpoint_path.unlink()
    else:
        logging.info(f"Checkpoint file not found at: {checkpoint_path}, nothing to delete.")
