"""
feature_engineer.py

Replicates EXACTLY the feature engineering done during training.
Training produced X_train of shape (25859, 12, 45) — 12 timesteps, 45 features.
This file takes raw price + macro data from the database and outputs
a numpy array of shape (1, 12, 45) ready for LSTM/CNN-LSTM inference.

CRITICAL: Every feature name, every window size, every formula must
match what was applied to create X_train.npy. The order of columns
must match feature_cols.pkl exactly. Any mismatch = garbage predictions.

Scaler format (from scalers.pkl):
    scalers['price_usd'] = {'min': float, 'max': float}
    Applied as: scaled = (value - min) / (max - min)
    Reversed as: value = scaled * (max - min) + min
"""

import numpy as np
import pandas as pd
import pickle
import logging
from pathlib import Path
from typing import Dict, List, Tuple, Optional

logger = logging.getLogger(__name__)


# ──────────────────────────────────────────────────────────────────────────────
# CONSTANTS — must match training exactly
# ──────────────────────────────────────────────────────────────────────────────

LOOKBACK = 12       # months of history fed to models
HORIZON  = 6        # months being predicted

# The 8 FRED macro indicators used during training (exact column names)
FRED_COLS = [
    'CPIAUCSL',       # Consumer Price Index — All Urban Consumers
    'GASREGW',        # Regular conventional gas price (weekly, resampled monthly)
    'UNRATE',         # Unemployment rate
    'UMCSENT',        # University of Michigan Consumer Sentiment
    'DCOILWTICO',     # Crude oil price WTI
    'CPIFABSL',       # CPI — Food and Beverages
    'CUSR0000SAF11',  # CPI — Food at Home
    'DEXUSEU',        # USD to EUR exchange rate
]

# Derived macro features computed from FRED cols
DERIVED_MACRO_COLS = [
    'cpi_mom_change',    # month-over-month change in CPIAUCSL
    'cpi_yoy_change',    # year-over-year change in CPIAUCSL
    'gas_7d_avg',        # 7-day rolling average of GASREGW (same as monthly here)
    'gas_30d_change',    # 30-day change in GASREGW
    'oil_30d_avg',       # 30-day rolling average of DCOILWTICO
]


# ──────────────────────────────────────────────────────────────────────────────
# FEATURE ENGINEER CLASS
# ──────────────────────────────────────────────────────────────────────────────

class FeatureEngineer:
    """
    Builds the 45-feature input matrix from raw price + macro data.
    Must be initialised once with the model artefacts from training.
    """

    def __init__(self, scalers: Dict, feature_cols: List[str]):
        """
        Args:
            scalers:      dict loaded from scalers.pkl
                          format: {'price_usd': {'min': x, 'max': x}, ...}
            feature_cols: list loaded from feature_cols.pkl
                          exactly 45 strings in the order used during training
        """
        self.scalers = scalers
        self.feature_cols = feature_cols
        self.n_features = len(feature_cols)

        if self.n_features != 45:
            logger.warning(
                f"Expected 45 features but feature_cols.pkl has {self.n_features}. "
                "Predictions may be incorrect if models were trained on 45."
            )

        logger.info(
            f"FeatureEngineer ready. "
            f"Features: {self.n_features}, Lookback: {LOOKBACK}, Horizon: {HORIZON}"
        )

    # ──────────────────────────────────────────────────────────────────────────
    # PUBLIC METHOD — call this from predictor.py
    # ──────────────────────────────────────────────────────────────────────────

    def build_input_sequence(
        self,
        price_history: pd.DataFrame,
        macro_history: pd.DataFrame,
        market_avg_prices: pd.Series,
    ) -> np.ndarray:
        """
        Builds a (1, 12, 45) input array for one product at one point in time.

        Args:
            price_history:    DataFrame with columns ['date', 'price_usd']
                              must have AT LEAST 24 months of history
                              (12 for lookback + 12 for lag calculations)
                              sorted ascending by date.

            macro_history:    DataFrame with columns:
                              ['date', 'CPIAUCSL', 'GASREGW', 'UNRATE',
                               'UMCSENT', 'DCOILWTICO', 'CPIFABSL',
                               'CUSR0000SAF11', 'DEXUSEU']
                              must cover the same date range as price_history.

            market_avg_prices: Series indexed by date containing the average
                               price across ALL products for that month.
                               Used for cross-product ratio features.

        Returns:
            np.ndarray of shape (1, 12, 45) — ready to pass to torch model.
            All values are scaled to [0, 1] using the training scalers.

        Raises:
            ValueError: if price_history has fewer than 24 rows.
        """
        logger.info("=" * 80)
        logger.info("START: build_input_sequence")
        logger.info(f"Input price_history shape: {price_history.shape}")
        logger.info(f"Input macro_history shape: {macro_history.shape}")
        logger.info(f"Input market_avg_prices shape: {market_avg_prices.shape}")

        if len(price_history) < 24:
            raise ValueError(
                f"Need at least 24 months of price history. Got {len(price_history)}."
            )

        # Step 1: build the full feature dataframe
        logger.info("STEP 1: Building full feature dataframe...")
        df = self._build_feature_dataframe(
            price_history, macro_history, market_avg_prices
        )
        logger.info(f"  After feature engineering, df shape: {df.shape}")
        logger.info(f"  Columns: {list(df.columns)}")

        # Step 2: take the last LOOKBACK rows (most recent 12 months)
        logger.info(f"STEP 2: Taking last {LOOKBACK} rows...")
        df_window = df.tail(LOOKBACK).reset_index(drop=True)
        logger.info(f"  df_window shape: {df_window.shape}")

        if len(df_window) < LOOKBACK:
            raise ValueError(
                f"After feature engineering, only {len(df_window)} rows remain. "
                f"Need {LOOKBACK}. Provide more historical data."
            )

        # Step 3: select features in exact training order
        logger.info(f"STEP 3: Selecting {self.n_features} features in training order...")
        missing = [c for c in self.feature_cols if c not in df_window.columns]
        if missing:
            logger.error(f"  MISSING COLUMNS: {missing}")
            raise ValueError(
                f"Missing features that were present at training time: {missing}. "
                "Check that price_history and macro_history contain required columns."
            )

        feature_matrix = df_window[self.feature_cols].values.astype(np.float32)  # (12, 45)
        logger.info(f"  Feature matrix shape before scaling: {feature_matrix.shape}")
        logger.info(f"  Feature matrix dtype: {feature_matrix.dtype}")
        logger.info(f"  Feature matrix min/max: [{feature_matrix.min():.4f}, {feature_matrix.max():.4f}]")
        logger.info(f"  Feature matrix has NaN: {np.isnan(feature_matrix).any()}")

        # Step 4: apply MinMax scaling column by column using training scalers
        logger.info(f"STEP 4: Applying MinMax scaling...")
        feature_matrix_scaled = self._apply_scaling(feature_matrix)
        logger.info(f"  Feature matrix shape after scaling: {feature_matrix_scaled.shape}")
        logger.info(f"  Feature matrix dtype after scaling: {feature_matrix_scaled.dtype}")
        logger.info(f"  Feature matrix min/max after scaling: [{feature_matrix_scaled.min():.4f}, {feature_matrix_scaled.max():.4f}]")
        logger.info(f"  Feature matrix has NaN after scaling: {np.isnan(feature_matrix_scaled).any()}")

        # Step 5: reshape to (1, 12, 45) for batch dimension
        logger.info(f"STEP 5: Reshaping to batch format...")
        result = feature_matrix_scaled[np.newaxis, :, :]   # (1, 12, 45)
        logger.info(f"  Final shape: {result.shape}")
        logger.info(f"  Final min/max: [{result.min():.4f}, {result.max():.4f}]")
        logger.info("=" * 80)
        return result


    def inverse_scale_price(self, scaled_values: np.ndarray) -> np.ndarray:
        """
        Reverses MinMax scaling on price predictions.
        scaled_value = (raw - min) / (max - min)
        raw_value    = scaled * (max - min) + min

        Args:
            scaled_values: array of scaled price values from model output

        Returns:
            array of USD prices
        """
        s = self.scalers['price_usd']
        return scaled_values * (s['max'] - s['min']) + s['min']


    # ──────────────────────────────────────────────────────────────────────────
    # PRIVATE METHODS — feature construction (mirrors training notebook)
    # ──────────────────────────────────────────────────────────────────────────

    def _build_feature_dataframe(
        self,
        price_history: pd.DataFrame,
        macro_history: pd.DataFrame,
        market_avg_prices: pd.Series,
    ) -> pd.DataFrame:
        """
        Constructs all 45 features in the same order as training.
        Returns a DataFrame where each row is one month.
        """

        # ── Merge price and macro on date ──
        df = price_history.copy()
        df['date'] = pd.to_datetime(df['date'])
        df = df.sort_values('date').reset_index(drop=True)

        macro = macro_history.copy()
        macro['date'] = pd.to_datetime(macro['date'])
        macro = macro.sort_values('date').reset_index(drop=True)

        df = pd.merge(df, macro, on='date', how='left')

        # ── Cross-product: market average price ──
        market_avg = market_avg_prices.copy()
        market_avg.index = pd.to_datetime(market_avg.index)
        df['market_avg_price'] = df['date'].map(market_avg)
        df['market_avg_price'] = df['market_avg_price'].fillna(
            df['price_usd'].mean()
        )

        # ── 1. PRICE LAG FEATURES ──
        # Exact lags used in training: 1, 2, 3, 6, 12 months
        for lag in [1, 2, 3, 6, 12]:
            df[f'price_lag_{lag}m'] = df['price_usd'].shift(lag)

        # ── 2. ROLLING STATISTICS ──
        # Windows: 3, 6, 12 months
        # Stats: mean, std, min, max
        for window in [3, 6, 12]:
            rolled = df['price_usd'].shift(1).rolling(window=window)
            df[f'rolling_mean_{window}m'] = rolled.mean()
            df[f'rolling_std_{window}m']  = rolled.std()
            df[f'rolling_min_{window}m']  = rolled.min()
            df[f'rolling_max_{window}m']  = rolled.max()

        # ── 3. RATE OF CHANGE FEATURES ──
        df['pct_change_1m']  = df['price_usd'].pct_change(1)
        df['pct_change_3m']  = df['price_usd'].pct_change(3)
        df['pct_change_6m']  = df['price_usd'].pct_change(6)
        df['pct_change_12m'] = df['price_usd'].pct_change(12)

        # Momentum: absolute dollar change over 3 and 12 months
        df['momentum_3m']  = df['price_usd'] - df['price_usd'].shift(3)
        df['momentum_12m'] = df['price_usd'] - df['price_usd'].shift(12)

        # Volatility: coefficient of variation over 12-month rolling window
        roll_12 = df['price_usd'].shift(1).rolling(12)
        df['volatility_12m'] = roll_12.std() / roll_12.mean()
        df['volatility_12m'] = df['volatility_12m'].replace([np.inf, -np.inf], 0)

        # ── 4. CALENDAR FEATURES ──
        df['month']   = df['date'].dt.month
        df['quarter'] = df['date'].dt.quarter

        # Sine/cosine encoding so model understands Jan and Dec are adjacent
        df['month_sin'] = np.sin(2 * np.pi * df['month'] / 12)
        df['month_cos'] = np.cos(2 * np.pi * df['month'] / 12)

        # Quarter sine/cosine encoding
        df['quarter_sin'] = np.sin(2 * np.pi * df['quarter'] / 4)
        df['quarter_cos'] = np.cos(2 * np.pi * df['quarter'] / 4)

        # Binary flag for Q4 (holiday shopping season)
        df['is_q4'] = (df['quarter'] == 4).astype(int)

        # ── 5. CROSS-PRODUCT FEATURES ──
        # Price relative to market basket average
        df['price_to_market_ratio'] = (
            df['price_usd'] / df['market_avg_price'].replace(0, np.nan)
        )
        df['price_to_market_ratio'] = df['price_to_market_ratio'].fillna(1.0)

        # ── 6. FRED MACRO FEATURES ──
        # Raw FRED columns are already merged from macro_history.
        # Forward-fill gaps (FRED has some missing months).
        for col in FRED_COLS:
            if col in df.columns:
                df[col] = df[col].ffill().bfill()
            else:
                # If a FRED column is missing, fill with zeros rather than crashing
                logger.warning(f"FRED column '{col}' not found. Filling with 0.")
                df[col] = 0.0

        # ── 7. DERIVED MACRO FEATURES ──
        # CPI month-over-month change
        df['cpi_mom_change'] = df['CPIAUCSL'].pct_change(1)

        # CPI year-over-year change
        df['cpi_yoy_change'] = df['CPIAUCSL'].pct_change(12)

        # Gas 7-day average (monthly data so this equals the monthly value)
        df['gas_7d_avg'] = df['GASREGW'].rolling(1).mean()

        # Gas 30-day (1-month) change
        df['gas_30d_change'] = df['GASREGW'].pct_change(1)

        # Oil 30-day average
        df['oil_30d_avg'] = df['DCOILWTICO'].rolling(1).mean()

        # ── 8. FILL REMAINING NaN ──
        # Lag and rolling features will have NaN at the start of the series.
        # Forward-fill then backward-fill to remove them.
        df = df.ffill().bfill().fillna(0)

        return df


    def _apply_scaling(self, feature_matrix: np.ndarray) -> np.ndarray:
        """
        Applies per-column MinMax scaling using the training scalers.

        If a scaler is not found for a column, that column is left unscaled
        and a warning is logged. This keeps the server running rather than
        crashing, though predictions for that column will be unscaled.

        Args:
            feature_matrix: (12, 45) raw feature values

        Returns:
            (12, 45) scaled to approximately [0, 1]
        """
        scaled = feature_matrix.copy().astype(np.float32)

        logger.debug("Scaling details (first 5 features):")
        for col_idx, col_name in enumerate(self.feature_cols):
            if col_name in self.scalers:
                s = self.scalers[col_name]
                col_min = s['min']
                col_max = s['max']
                denom   = col_max - col_min

                raw_min = scaled[:, col_idx].min()
                raw_max = scaled[:, col_idx].max()

                if denom == 0:
                    scaled[:, col_idx] = 0.5
                    if col_idx < 5:
                        logger.debug(f"  [{col_idx}] {col_name}: zero variance → 0.5")
                else:
                    scaled[:, col_idx] = (
                        (scaled[:, col_idx] - col_min) / denom
                    )
                    scaled[:, col_idx] = np.clip(scaled[:, col_idx], 0.0, 1.0)
                    if col_idx < 5:
                        logger.debug(
                            f"  [{col_idx}] {col_name}: raw [{raw_min:.4f}, {raw_max:.4f}] "
                            f"→ train range [{col_min:.4f}, {col_max:.4f}] "
                            f"→ scaled [{scaled[:, col_idx].min():.4f}, {scaled[:, col_idx].max():.4f}]"
                        )
            else:
                logger.warning(
                    f"No scaler found for column '{col_name}' (index {col_idx}). "
                    "This column will not be scaled. "
                    "Verify this column was present during training."
                )

        return scaled


# ──────────────────────────────────────────────────────────────────────────────
# LOADER UTILITY — call once at FastAPI startup
# ──────────────────────────────────────────────────────────────────────────────

def load_feature_engineer(models_dir: str) -> FeatureEngineer:
    """
    Loads scalers.pkl and feature_cols.pkl and returns a ready FeatureEngineer.

    Args:
        models_dir: path to the folder containing scalers.pkl and feature_cols.pkl
                    (the same folder your .pth files live in)

    Returns:
        FeatureEngineer instance

    Raises:
        FileNotFoundError if either pkl file is missing.
    """
    models_path = Path(models_dir)

    scalers_path      = models_path / 'scalers.pkl'
    feature_cols_path = models_path / 'feature_cols.pkl'

    if not scalers_path.exists():
        raise FileNotFoundError(f"scalers.pkl not found at {scalers_path}")
    if not feature_cols_path.exists():
        raise FileNotFoundError(f"feature_cols.pkl not found at {feature_cols_path}")

    with open(scalers_path, 'rb') as f:
        scalers = pickle.load(f)

    with open(feature_cols_path, 'rb') as f:
        feature_cols = pickle.load(f)

    logger.info(f"Loaded scalers for {len(scalers)} columns.")
    logger.info(f"Loaded feature_cols: {len(feature_cols)} features.")
    logger.info(f"Feature order: {feature_cols}")

    return FeatureEngineer(scalers=scalers, feature_cols=feature_cols)


# ──────────────────────────────────────────────────────────────────────────────
# QUICK SMOKE TEST
# Run this file directly to verify it works before wiring it into FastAPI:
#   python feature_engineer.py
# ──────────────────────────────────────────────────────────────────────────────

if __name__ == '__main__':
    import os

    logging.basicConfig(level=logging.INFO)

    # ── Point this at your local models folder ──
    MODELS_DIR = os.environ.get('MODELS_DIR', './models')

    print(f"\nLoading artefacts from: {MODELS_DIR}")
    fe = load_feature_engineer(MODELS_DIR)

    print(f"\nFeature columns ({fe.n_features} total):")
    for i, col in enumerate(fe.feature_cols):
        print(f"  [{i:02d}] {col}")

    # ── Build synthetic data matching real data format ──
    dates = pd.date_range(end='2026-01-01', periods=36, freq='MS')

    price_history = pd.DataFrame({
        'date':      dates,
        'price_usd': np.random.uniform(2.0, 6.0, len(dates)),
    })

    macro_history = pd.DataFrame({'date': dates})
    macro_defaults = {
        'CPIAUCSL': 310.0, 'GASREGW': 3.5,   'UNRATE': 4.0,
        'UMCSENT':  70.0,  'DCOILWTICO': 80.0, 'CPIFABSL': 320.0,
        'CUSR0000SAF11': 290.0, 'DEXUSEU': 1.08,
    }
    for col, val in macro_defaults.items():
        macro_history[col] = val + np.random.normal(0, 0.1, len(dates))

    market_avg = pd.Series(
        np.random.uniform(3.0, 5.0, len(dates)), index=dates
    )

    print("\nBuilding input sequence...")
    try:
        X = fe.build_input_sequence(price_history, macro_history, market_avg)
        print(f"\nOutput shape: {X.shape}")
        print(f"Expected:     (1, 12, 45)")
        assert X.shape == (1, 12, 45), f"SHAPE MISMATCH: got {X.shape}"
        print(f"\nValue range: min={X.min():.4f}  max={X.max():.4f}")
        print(f"Expected range: approximately [0, 1]")
        assert X.min() >= -0.1 and X.max() <= 1.1, "Values outside expected [0,1] range"
        print("\n✓ Smoke test PASSED. feature_engineer.py is working correctly.")
    except Exception as e:
        print(f"\n✗ Smoke test FAILED: {e}")
        raise