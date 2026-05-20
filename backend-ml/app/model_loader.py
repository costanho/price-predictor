import logging
import pickle
from pathlib import Path
import torch
import torch.nn as nn

logger = logging.getLogger(__name__)


class LSTM_Model(nn.Module):
    def __init__(self, input_size=45, hidden_size=128, output_size=6):
        super().__init__()
        self.lstm = nn.LSTM(input_size, hidden_size, num_layers=2, batch_first=True)
        self.bn = nn.BatchNorm1d(hidden_size)
        self.fc = nn.Linear(hidden_size, output_size)

    def forward(self, x):
        lstm_out, _ = self.lstm(x)
        last_output = lstm_out[:, -1, :]
        bn_out = self.bn(last_output)
        output = self.fc(bn_out)
        return output


class _PassthroughModel(nn.Module):
    """Model that passes through input (identity) for unused models."""
    def __init__(self):
        super().__init__()

    def forward(self, x):
        return x  # Return input as-is

    def load_state_dict(self, state_dict, strict=True):
        pass  # Ignore state dict, this is a dummy


MODELS_DIR = Path(__file__).parent.parent / "models"

models = {
    "lstm": None,
    "cnn_lstm": None,
    "tft": None,
    "autoencoder": None,
    "ensemble": None,
    "scalers": None,
    "feature_cols": None,
}


class ModelLoader:
    @staticmethod
    async def load_all():
        """Load all models and return the dictionary."""
        results = models.copy()

        results["lstm"] = ModelLoader._load_pytorch("best_lstm.pth")
        results["cnn_lstm"] = ModelLoader._load_pytorch("best_cnn_lstm.pth")
        results["autoencoder"] = ModelLoader._load_pytorch("best_autoencoder.pth")

        try:
            results["tft"] = ModelLoader._load_darts_tft("best_tft_darts")
        except Exception as e:
            logger.warning(f"TFT model load failed (non-critical): {e}. Server will continue without TFT.")
            results["tft"] = None

        results["ensemble"] = ModelLoader._load_pickle("ensemble_model.pkl")
        results["scalers"] = ModelLoader._load_pickle("scalers.pkl")
        results["feature_cols"] = ModelLoader._load_pickle("feature_cols.pkl")

        return results

    @staticmethod
    def _load_pytorch(filename):
        """Load a PyTorch model file."""
        filepath = MODELS_DIR / filename
        try:
            loaded = torch.load(filepath, map_location="cpu", weights_only=False)
            logger.info(f"Successfully loaded {filename}")

            if isinstance(loaded, dict):
                logger.info(f"  {filename} is a state dict, creating model and loading weights")
                model = ModelLoader._create_model(filename, loaded)
                try:
                    model.load_state_dict(loaded, strict=True)
                except RuntimeError as e:
                    logger.warning(f"Could not load state dict strictly, trying with strict=False: {str(e)[:80]}")
                    model.load_state_dict(loaded, strict=False)
                model.eval()
                return model
            return loaded
        except FileNotFoundError:
            logger.error(f"Model file not found: {filepath}")
            raise
        except Exception as e:
            logger.error(f"Failed to load {filename}: {type(e).__name__}: {e}")
            raise

    @staticmethod
    def _create_model(filename, state_dict):
        """Create a model instance based on filename."""
        if "lstm" in filename and "cnn" not in filename:
            input_size = state_dict.get('lstm.weight_ih_l0', torch.zeros(1, 45)).shape[1]
            hidden_size = state_dict.get('lstm.weight_hh_l0', torch.zeros(512, 128)).shape[1]
            output_size = state_dict.get('fc.weight', torch.zeros(6, 128)).shape[0]
            logger.info(f"  LSTM dimensions: input={input_size}, hidden={hidden_size}, output={output_size}")
            return LSTM_Model(input_size=input_size, hidden_size=hidden_size, output_size=output_size)
        else:
            # For CNN_LSTM and autoencoder, use passthrough since only LSTM is used in predictor
            return _PassthroughModel()

    @staticmethod
    def _load_darts_tft(folder_name):
        """Load Darts TFT model from folder."""
        filepath = MODELS_DIR / folder_name
        try:
            from darts.models import TFTModel
            model = TFTModel.load(str(filepath))
            logger.info(f"Successfully loaded TFT model from {folder_name}")
            return model
        except Exception:
            return None

    @staticmethod
    def _load_pickle(filename):
        """Load a pickle file."""
        filepath = MODELS_DIR / filename
        try:
            with open(filepath, "rb") as f:
                data = pickle.load(f)
            logger.info(f"Successfully loaded {filename}")
            return data
        except FileNotFoundError:
            logger.error(f"Pickle file not found: {filepath}")
            raise
        except Exception as e:
            logger.error(f"Failed to load {filename}: {type(e).__name__}: {e}")
            raise
