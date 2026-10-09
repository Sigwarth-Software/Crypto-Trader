# preprocessor.py
import logging

import numpy as np
import pandas as pd
import joblib
from attr import attr, Factory
from attrs import define
from dataclasses import dataclass
from numpy.lib.stride_tricks import sliding_window_view
from sklearn.preprocessing import MinMaxScaler
from datetime import datetime


# ---------------------------------------------------------------------------
# Data container returned by the shared preparation step
# ---------------------------------------------------------------------------

# TODO: Move to its own file.

@dataclass
class PreparedData:
    target_price: np.ndarray    # (T,) raw prices, float32
    raw_returns: np.ndarray     # (T, F) pct-change features, float32
    scaled_returns: np.ndarray  # (T, F) MinMax-scaled features
    target_returns: np.ndarray  # (T,) pct-change of target, float32
    total_rows: int             # T
    num_features: int           # F


# ---------------------------------------------------------------------------
# Module-level utility functions
# ---------------------------------------------------------------------------

def _zscore_normalize(channel: np.ndarray) -> np.ndarray:
    """Z-score normalize a (N, seq) array per-sample to avoid data leakage.

    Each sample window is independently normalized to zero mean and unit
    variance, ensuring no information from future samples leaks into past.
    """
    # Per-sample normalization: (N, 1)
    mean = channel.mean(axis=1, keepdims=True)
    std = channel.std(axis=1, keepdims=True)
    std = np.where(std == 0, 1.0, std)
    return ((channel - mean) / std).astype(np.float32)


def build_sliding_windows_2d(
    data: np.ndarray,
    seq_len: int,
    prefix_len: int,
    offset: int,
    num_samples: int,
) -> np.ndarray:
    """Build (N, seq_len, F) windows from a (T, F) array, aligned by offset."""
    window_view = sliding_window_view(data[:prefix_len], window_shape=seq_len, axis=0)
    window_view = window_view.transpose(0, 2, 1)  # (prefix_len - seq_len + 1, seq_len, F)
    return window_view[offset: offset + num_samples]


def build_sliding_windows_1d(
    data: np.ndarray,
    seq_len: int,
    prefix_len: int,
    offset: int,
    num_samples: int,
) -> np.ndarray:
    """Build (N, seq_len) windows from a (T,) array, aligned by offset."""
    window_view = sliding_window_view(data[:prefix_len], window_shape=seq_len, axis=0)
    return window_view[offset: offset + num_samples].astype(np.float64)


def build_downsampled_history_windows(
    price_series: np.ndarray,
    seq_len: int,
    prefix_len: int,
    offset: int,
    num_samples: int,
    stride: int = 5,
) -> np.ndarray:
    """
    Build (N, seq_len) windows of downsampled price history.

    Takes a 2×seq_len price window, strides by `stride`, and pads/trims
    to exactly seq_len.  Left-padding uses each sample's own earliest
    available price rather than the global first price.
    """
    # Build 2*seq_len windows first, then downsample per-sample
    padded_len = 2 * seq_len
    total_needed = prefix_len + padded_len  # max index we might need

    # Build sliding windows of length 2*seq_len over available data
    # For early samples, we pad on the left with the sample's own first price
    results = np.empty((num_samples, seq_len), dtype=np.float64)

    for i in range(num_samples):
        # The endpoint for this sample
        end_idx = offset + i + seq_len  # end of the seq_len window in price_series
        # We want 2*seq_len of history ending at end_idx
        start_idx = end_idx - padded_len

        if start_idx >= 0 and end_idx <= prefix_len:
            window = price_series[start_idx:end_idx]
        else:
            # Need left-padding
            actual_start = max(0, start_idx)
            actual_end = min(end_idx, prefix_len)
            actual_data = price_series[actual_start:actual_end]
            pad_needed = padded_len - len(actual_data)
            if pad_needed > 0:
                # Pad with the sample's own earliest available price
                pad_val = actual_data[0] if len(actual_data) > 0 else 0.0
                window = np.concatenate([np.full(pad_needed, pad_val), actual_data])
            else:
                window = actual_data[-padded_len:]

        # Downsample by stride
        downsampled = window[::stride]

        # Pad or trim to seq_len
        if len(downsampled) < seq_len:
            pad = np.full(seq_len - len(downsampled), downsampled[-1] if len(downsampled) > 0 else 0.0)
            downsampled = np.concatenate([downsampled, pad])
        elif len(downsampled) > seq_len:
            downsampled = downsampled[-seq_len:]

        results[i] = downsampled

    return results


def compute_windowed_pearson_correlation(
    feature_windows: np.ndarray,
    target_windows: np.ndarray,
    rolling_size: int = 5,
) -> np.ndarray:
    """
    Rolling Pearson correlation between features and target within each
    sample window.

    Instead of computing a single scalar per sample (which collapses to a
    constant across time steps), this computes a rolling correlation using
    a sub-window of `rolling_size` steps, giving the LSTM a time-varying
    correlation signal.

    Parameters
    ----------
    feature_windows : (N, seq, F) raw return windows
    target_windows  : (N, seq) target return windows
    rolling_size    : size of the rolling sub-window for correlation

    Returns
    -------
    correlation_channel : (N, seq) time-varying mean correlation
    """
    N, seq, F = feature_windows.shape
    result = np.zeros((N, seq), dtype=np.float32)

    # Clamp rolling_size to be at most seq
    rs = min(rolling_size, seq)

    for t in range(seq):
        # Sub-window: [max(0, t-rs+1) : t+1]
        start = max(0, t - rs + 1)
        end = t + 1
        window_len = end - start

        if window_len < 2:
            # Not enough points for correlation, use 0
            result[:, t] = 0.0
            continue

        A = feature_windows[:, start:end, :]    # (N, window_len, F)
        B = target_windows[:, start:end]         # (N, window_len)

        A_mean = A.mean(axis=1, keepdims=True)   # (N, 1, F)
        B_mean = B.mean(axis=1, keepdims=True)   # (N, 1)
        A_c = A - A_mean                         # (N, window_len, F)
        B_c = (B - B_mean)[..., None]            # (N, window_len, 1)

        num = (A_c * B_c).sum(axis=1)            # (N, F)
        den = (
            np.sqrt((A_c ** 2).sum(axis=1) * (B_c ** 2).sum(axis=1))
            + 1e-12
        )
        corr = np.clip(num / den, -1.0, 1.0)    # (N, F)
        result[:, t] = corr.mean(axis=1).astype(np.float32)  # (N,)

    return result


def build_engineered_channels(
    target_price_windows: np.ndarray,
    downsampled_history: np.ndarray,
    correlation_channel: np.ndarray,
) -> np.ndarray:
    """
    Z-score-normalize price-based channels and stack into a (N, seq, 3) tensor.

    No pre-multiplication by weights — the model learns the relative
    importance of each channel through its own parameters.

    Channel 0: recent price trend (z-scored per sample)
    Channel 1: downsampled longer history (z-scored per sample)
    Channel 2: cross-asset rolling correlation (already bounded [-1,1])
    """
    past_norm = _zscore_normalize(target_price_windows)
    key_norm = _zscore_normalize(downsampled_history)
    corr_norm = correlation_channel.astype(np.float32)

    N, seq = target_price_windows.shape
    if not (past_norm.shape == key_norm.shape == corr_norm.shape == (N, seq)):
        raise ValueError(
            f"Engineered channel shape mismatch: past{past_norm.shape} "
            f"key{key_norm.shape} corr{corr_norm.shape} expected {(N, seq)}")

    return np.stack([past_norm, key_norm, corr_norm], axis=-1)


# ---------------------------------------------------------------------------
# Main Preprocessor class
# ---------------------------------------------------------------------------

@define
class Preprocessor:
    sequence_length: int = attr(default=10)
    future_steps: int = attr(default=1)
    input_scaler: MinMaxScaler = attr(default=Factory(lambda: MinMaxScaler(feature_range=(0, 1))))

    # ------------------------------------------------------------------
    # Shared preparation: validate, sort, pct_change, scale
    # ------------------------------------------------------------------

    def _prepare_dataframe(self, dataframe, target_currency: str) -> PreparedData:
        """
        Validate, sort chronologically, compute pct-change returns for
        input features, and fit/apply the MinMaxScaler. Returns a
        PreparedData container with all arrays needed downstream.
        """
        logging.info(f"Preparing dataframe for target currency: {target_currency}")
        if dataframe.empty:
            raise ValueError(
                "Error: The dataframe is empty. No data retrieved from database.")

        target_column = f"{target_currency.lower()}_price"

        # --- robust sort by datetime ---
        df = dataframe.copy()
        if "last_updated" not in df.columns:
            raise ValueError("Missing 'last_updated' column.")
        if not np.issubdtype(df["last_updated"].dtype, np.datetime64):
            df["last_updated"] = pd.to_datetime(df["last_updated"], errors="coerce")
        df = df.sort_values("last_updated").reset_index(drop=True)

        # --- select numeric columns ---
        numeric_cols = df.select_dtypes(include=["number"]).columns
        if target_column not in numeric_cols:
            raise ValueError(
                f"Missing target column '{target_column}' in numeric columns.")
        raw_input_cols = [c for c in numeric_cols if c != target_column]

        # --- raw features as returns; keep target as price ---
        df[raw_input_cols] = df[raw_input_cols].pct_change(fill_method=None)
        df[raw_input_cols] = df[raw_input_cols].replace([np.inf, -np.inf], np.nan).fillna(0.0)

        target_price = df[target_column].to_numpy(dtype=np.float32)
        raw_returns = df[raw_input_cols].to_numpy(dtype=np.float32)

        # Target returns for correlation channel
        target_returns = df[target_column].pct_change(fill_method=None).to_numpy(dtype=np.float32)
        # Drop the first NaN and align all arrays
        target_returns = target_returns[1:]
        target_price = target_price[1:]
        raw_returns = raw_returns[1:]

        T = len(target_price)
        F = raw_returns.shape[1]

        # --- scale ONLY the raw returns ---
        if hasattr(self.input_scaler, "scale_"):
            scaled_returns = self.input_scaler.transform(raw_returns)
        else:
            scaled_returns = self.input_scaler.fit_transform(raw_returns)

        return PreparedData(
            target_price=target_price,
            raw_returns=raw_returns,
            scaled_returns=scaled_returns,
            target_returns=target_returns,
            total_rows=T,
            num_features=F,
        )

    # ------------------------------------------------------------------
    # Build features for a single temporal scale
    # ------------------------------------------------------------------

    def _build_single_scale(
        self,
        data: PreparedData,
        seq_len: int,
        prefix_len: int,
        offset: int,
        num_samples: int,
    ) -> np.ndarray:
        """
        Build the (N, seq_len, 3+F) feature tensor for one temporal scale.

        Parameters
        ----------
        data        : PreparedData from _prepare_dataframe
        seq_len     : lookback window size for this scale
        prefix_len  : T - future_steps (windows must end before the label)
        offset      : alignment offset (L - seq_len for multi-scale, 0 for single)
        num_samples : N, number of output samples
        """
        # Raw return windows (unscaled) for Pearson correlation
        unscaled_return_windows = build_sliding_windows_2d(
            data.raw_returns, seq_len, prefix_len, offset, num_samples
        ).astype(np.float64)  # (N, seq_len, F)

        # Scaled return windows for final X
        scaled_return_windows = build_sliding_windows_2d(
            data.scaled_returns, seq_len, prefix_len, offset, num_samples
        ).astype(np.float32)  # (N, seq_len, F)

        # Target price windows for "past trends" channel
        target_price_windows = build_sliding_windows_1d(
            data.target_price, seq_len, prefix_len, offset, num_samples
        )  # (N, seq_len)

        # Target return windows for correlation
        target_return_windows = build_sliding_windows_1d(
            data.target_returns, seq_len, prefix_len, offset, num_samples
        )  # (N, seq_len)

        # Rolling Pearson correlation channel (time-varying)
        correlation_channel = compute_windowed_pearson_correlation(
            unscaled_return_windows, target_return_windows
        )  # (N, seq_len)

        # Downsampled history channel
        downsampled_history = build_downsampled_history_windows(
            data.target_price, seq_len, prefix_len, offset, num_samples
        )  # (N, seq_len)

        # Combine into 3 engineered channels (no pre-weighting)
        engineered_channels = build_engineered_channels(
            target_price_windows, downsampled_history, correlation_channel
        )  # (N, seq_len, 3)

        # Concatenate engineered channels with scaled raw returns
        return np.concatenate(
            [engineered_channels, scaled_return_windows], axis=-1
        )  # (N, seq_len, 3+F)

    # ------------------------------------------------------------------
    # Public API: single-scale transform
    # ------------------------------------------------------------------

    def transform(self, dataframe, target_currency):
        data = self._prepare_dataframe(dataframe, target_currency)

        seq = self.sequence_length
        fwd = self.future_steps
        N = data.total_rows - seq - fwd
        if N <= 0:
            raise ValueError("Not enough data to form a single sample.")

        prefix_len = data.total_rows - fwd

        X = self._build_single_scale(data, seq, prefix_len, offset=0, num_samples=N)
        y = data.target_price[seq + fwd: seq + fwd + N].astype(np.float32)

        # --- sanity ---
        if X.ndim != 3:
            raise ValueError(f"Shape mismatch in X => {X.shape}")
        if y.ndim != 1 or y.shape[0] != N:
            raise ValueError(f"Shape mismatch in y => {y.shape}, expected ({N},)")

        return X, y, self.input_scaler

    # ------------------------------------------------------------------
    # Public API: multi-scale transform
    # ------------------------------------------------------------------

    def transform_multi_scale_with_weights(
            self,
            dataframe,
            target_currency: str,
            short_seq: int = 10,
            medium_seq: int = 50,
            long_seq: int = 150,
    ):
        data = self._prepare_dataframe(dataframe, target_currency)

        fwd = self.future_steps
        L = max(short_seq, medium_seq, long_seq)  # longest window
        prefix_len = data.total_rows - fwd
        if prefix_len < L:
            raise ValueError(
                "Not enough data to form short, medium, and long windows.")

        # Common N across all scales (align endpoints to the longest window)
        N = prefix_len - L + 1

        X_short = self._build_single_scale(data, short_seq, prefix_len, offset=L - short_seq, num_samples=N)
        X_medium = self._build_single_scale(data, medium_seq, prefix_len, offset=L - medium_seq, num_samples=N)
        X_long = self._build_single_scale(data, long_seq, prefix_len, offset=L - long_seq, num_samples=N)

        # Labels aligned to endpoints
        y = data.target_price[(L - 1) + fwd: (L - 1) + fwd + N].astype(np.float32)

        # --- sanity ---
        if y.ndim != 1 or y.shape[0] != X_long.shape[0]:
            raise ValueError(
                f"Label shape mismatch: y{y.shape} vs N{X_long.shape[0]}")
        if not (X_short.shape[0] == X_medium.shape[0] == X_long.shape[0] == N):
            raise ValueError("N mismatch among scales.")

        return X_short, X_medium, X_long, y, self.input_scaler

    # ------------------------------------------------------------------
    # Scaler persistence (Issue #8 fix)
    # ------------------------------------------------------------------

    def save_scalers(self, path_prefix: str) -> None:
        """Save the input_scaler to disk for later inference use."""
        joblib.dump(self.input_scaler, f"{path_prefix}_input_scaler.pkl")

    @classmethod
    def load_scalers(cls, path_prefix: str) -> MinMaxScaler:
        """Load a previously saved input_scaler from disk."""
        return joblib.load(f"{path_prefix}_input_scaler.pkl")

    # ------------------------------------------------------------------
    # Utility
    # ------------------------------------------------------------------

    @staticmethod
    def get_steps_from_now(future_time: datetime) -> int:
        future_time_seconds: float = future_time.timestamp() - datetime.now().timestamp()
        return int(future_time_seconds / 5)
