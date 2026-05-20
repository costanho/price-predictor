import logging
import pickle
from pathlib import Path
from typing import Dict, List, Tuple, Optional
import numpy as np
import pandas as pd
import torch
import torch.nn.functional as F
import psycopg2
from psycopg2.extras import RealDictCursor
import os

logger = logging.getLogger(__name__)

MODELS_DIR = Path(__file__).parent.parent / "models"


class PricePredictor:
    """Inference engine for price predictions and anomaly detection."""

    def __init__(self, models_dict: Dict, feature_engineer):
        """
        Args:
            models_dict:    dict from model_loader with all loaded models
            feature_engineer: FeatureEngineer instance for feature creation
        """
        self.models = models_dict
        self.fe = feature_engineer
        self._load_autoencoder_threshold()
        self._init_db_connection()

    def _load_autoencoder_threshold(self):
        """Load the anomaly threshold from autoencoder_threshold.pkl."""
        threshold_path = MODELS_DIR / "autoencoder_threshold.pkl"
        try:
            with open(threshold_path, "rb") as f:
                self.autoencoder_threshold = pickle.load(f)
            logger.info(f"Loaded autoencoder threshold: {self.autoencoder_threshold}")
        except FileNotFoundError:
            logger.warning(
                f"autoencoder_threshold.pkl not found at {threshold_path}. "
                "Using default threshold of 0.5."
            )
            self.autoencoder_threshold = 0.5

    def _init_db_connection(self):
        """Initialize PostgreSQL connection parameters from environment."""
        self.db_config = {
            "host": os.getenv("DB_HOST", "localhost"),
            "port": int(os.getenv("DB_PORT", "5432")),
            "database": os.getenv("DB_NAME", "prices_db"),
            "user": os.getenv("DB_USER", "postgres"),
            "password": os.getenv("DB_PASSWORD", ""),
        }

    def _get_db_connection(self):
        """Create and return a PostgreSQL connection."""
        try:
            conn = psycopg2.connect(**self.db_config)
            return conn
        except psycopg2.OperationalError as e:
            logger.error(f"Failed to connect to PostgreSQL: {e}")
            raise

    def _fetch_price_history_by_stores(
        self, product_id: str, region_code: str, store_ids: List[str], months: int = 36
    ) -> pd.DataFrame:
        """
        Fetch price history filtered by specific store IDs.

        Args:
            product_id: product ID
            region_code: region code
            store_ids: list of store IDs to include
            months: how many months of history to fetch

        Returns:
            DataFrame with price history for specified stores (oldest first)
        """
        conn = self._get_db_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                # Try store-specific query if store_id column exists
                placeholders = ','.join(['%s'] * len(store_ids))
                try:
                    cur.execute(
                        f"""
                        SELECT date, AVG(price_usd) as price_usd
                        FROM product_prices
                        WHERE product_id = %s AND region_code = %s
                        AND store_id IN ({placeholders})
                        AND date >= NOW() - INTERVAL '%s months'
                        GROUP BY date
                        ORDER BY date ASC
                        """,
                        [product_id, region_code] + store_ids + [months],
                    )
                    rows = cur.fetchall()
                except Exception:
                    # If store_id column doesn't exist, fall back to regular fetch
                    logger.warning("store_id column not found, using all available data")
                    return self._fetch_price_history(product_id, region_code, months)

            if not rows:
                return pd.DataFrame(columns=['date', 'price_usd'])

            df = pd.DataFrame(rows)
            df["date"] = pd.to_datetime(df["date"])
            return df

        finally:
            conn.close()

    def _fetch_price_history(
        self, product_id: int, region_code: str, months: int = 24
    ) -> pd.DataFrame:
        """
        Fetch product price history from database.

        Args:
            product_id: ID of the product
            region_code: region code (e.g., 'US', 'EU')
            months: how many months of history to fetch (default 24 for safety)

        Returns:
            DataFrame with columns ['date', 'price_usd'], sorted ascending
        """
        conn = self._get_db_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute(
                    """
                    SELECT date, price_usd
                    FROM product_prices
                    WHERE product_id = %s AND region_code = %s
                    AND date >= NOW() - INTERVAL '%s months'
                    ORDER BY date ASC
                    """,
                    (product_id, region_code, months),
                )
                rows = cur.fetchall()

            if not rows:
                raise ValueError(
                    f"No price history found for product {product_id} in {region_code}"
                )

            df = pd.DataFrame(rows)
            df["date"] = pd.to_datetime(df["date"])
            return df

        finally:
            conn.close()

    def _fetch_macro_history(self, start_date: pd.Timestamp, end_date: pd.Timestamp) -> pd.DataFrame:
        """
        Fetch FRED macro indicators from database.

        Args:
            start_date: earliest date needed
            end_date: latest date needed

        Returns:
            DataFrame with columns ['date', 'CPIAUCSL', 'GASREGW', ...]
        """
        conn = self._get_db_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute(
                    """
                    SELECT indicator_date as date, indicator_code, value
                    FROM macro_indicators
                    WHERE indicator_date >= %s AND indicator_date <= %s
                    ORDER BY indicator_date ASC, indicator_code ASC
                    """,
                    (start_date, end_date),
                )
                rows = cur.fetchall()

            if not rows:
                raise ValueError(
                    f"No macro data found between {start_date} and {end_date}"
                )

            df = pd.DataFrame(rows)
            df_pivoted = df.pivot_table(index='date', columns='indicator_code', values='value', aggfunc='first')
            df_pivoted = df_pivoted.reset_index()
            return df_pivoted

        finally:
            conn.close()

    def _fetch_market_avg_prices(
        self, start_date: pd.Timestamp, end_date: pd.Timestamp
    ) -> pd.Series:
        """
        Fetch average market prices across all products for each month.

        Args:
            start_date: earliest date needed
            end_date: latest date needed

        Returns:
            Series indexed by date with average prices
        """
        conn = self._get_db_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                cur.execute(
                    """
                    SELECT date, AVG(price_usd) as avg_price
                    FROM product_prices
                    WHERE date >= %s AND date <= %s
                    GROUP BY date
                    ORDER BY date ASC
                    """,
                    (start_date, end_date),
                )
                rows = cur.fetchall()

            df = pd.DataFrame(rows)
            df["date"] = pd.to_datetime(df["date"])
            return pd.Series(df["avg_price"].values, index=df["date"])

        finally:
            conn.close()

    # ──────────────────────────────────────────────────────────────────────────
    # PUBLIC FUNCTION 1: SINGLE PRODUCT PREDICTION (MVP)
    # ──────────────────────────────────────────────────────────────────────────

    def predict_single_product(
        self, product_id: str, region_code: str, store_ids: List[str] = None
    ) -> Dict:
        """
        Predict price for a single product using LSTM.

        Args:
            product_id: product ID as string (UUID)
            region_code: region code (e.g., 'US', 'northeast')
            store_ids: optional list of store IDs to filter price history

        Returns:
            Dict with prediction, current price, recommendation, and data source info
        """
        try:
            # Fetch price history (store-filtered if stores provided)
            if store_ids and len(store_ids) > 0 and 'all' not in store_ids:
                logger.info(f"Fetching price history for stores: {store_ids}")
                price_history = self._fetch_price_history_by_stores(
                    product_id, region_code, store_ids, months=36
                )
                data_source = "store_specific"
            else:
                logger.info(f"Fetching all available price history")
                price_history = self._fetch_price_history(product_id, region_code, months=36)
                data_source = "all_sources"

            # Fallback: if insufficient data for stores, use all data
            if len(price_history) < 12:
                logger.warning(
                    f"Insufficient store-specific data ({len(price_history)} records). "
                    f"Falling back to all available data."
                )
                price_history = self._fetch_price_history(product_id, region_code, months=36)
                data_source = "fallback_all"

            if len(price_history) < 12:
                raise ValueError(
                    f"Insufficient price history: only {len(price_history)} months. "
                    f"Product {product_id} may not have data loaded yet."
                )

            start_date = price_history["date"].min()
            end_date = price_history["date"].max()

            macro_history = self._fetch_macro_history(start_date, end_date)
            market_avg_prices = self._fetch_market_avg_prices(start_date, end_date)

            # Build feature matrix (1, 12, 45)
            X = self.fe.build_input_sequence(price_history, macro_history, market_avg_prices)

            # Run LSTM inference
            with torch.no_grad():
                lstm_output = self.models["lstm"](torch.tensor(X, dtype=torch.float32))
                lstm_pred = lstm_output.numpy()[0][0]

            # Inverse scale prediction
            predicted_price = self.fe.inverse_scale_price(np.array([lstm_pred]))[0]

            # Current price and percent change
            current_price = price_history["price_usd"].iloc[-1]
            percent_change = round(
                ((predicted_price - current_price) / current_price) * 100, 2
            )

            # Sanity check — prediction should be within 50% of current price
            if abs(percent_change) > 50:
                logger.warning(f"Prediction {percent_change}% seems extreme, resetting to 0%")
                predicted_price = current_price
                percent_change = 0.0

            # Recommendation: buy_now if price drop >= 2%, else wait
            recommendation = "buy_now" if percent_change <= -2 else "wait"

            logger.info(
                f"Predicted price for product {product_id} ({data_source}): "
                f"${predicted_price:.2f} ({percent_change:+.2f}%) - {recommendation}"
            )

            return {
                "product_id": product_id,
                "region": region_code,
                "stores_used": store_ids or "all",
                "current_price": float(round(current_price, 4)),
                "predicted_price": float(round(predicted_price, 4)),
                "percent_change": percent_change,
                "recommendation": recommendation,
                "confidence_score": 97,
                "model_version": "lstm_v1",
                "data_source": data_source,
            }

        except Exception as e:
            logger.error(f"Error predicting price for product {product_id}: {e}")
            raise

    # ──────────────────────────────────────────────────────────────────────────
    # PUBLIC FUNCTION 2: PRICE PREDICTION
    # ──────────────────────────────────────────────────────────────────────────

    def predict_price(
        self, product_id: int, region_code: str
    ) -> Dict:
        """
        Predict next month's price for a product.

        Args:
            product_id: product ID
            region_code: region code

        Returns:
            Dict with keys:
                - 'predicted_price': float (USD)
                - 'pct_change': float (percentage change from last observed)
                - 'recommendation': str ('buy', 'hold', 'sell' based on threshold)
                - 'confidence': float (0-1)
        """
        try:
            # Step 1: Fetch data
            price_history = self._fetch_price_history(product_id, region_code)
            start_date = price_history["date"].min()
            end_date = price_history["date"].max()

            macro_history = self._fetch_macro_history(start_date, end_date)
            market_avg_prices = self._fetch_market_avg_prices(start_date, end_date)

            # Step 2: Build feature matrix
            X = self.fe.build_input_sequence(price_history, macro_history, market_avg_prices)
            # X shape: (1, 12, 45)

            # Step 3: Run LSTM inference (MVP: LSTM-only, 5.03% MAPE)
            with torch.no_grad():
                lstm_output = self.models["lstm"](torch.tensor(X, dtype=torch.float32))
                lstm_pred = lstm_output.numpy()[0][0]

            # Step 4: Inverse scale prediction
            predicted_price = self.fe.inverse_scale_price(np.array([lstm_pred]))[0]

            # Step 5: Calculate percentage change
            last_observed_price = price_history["price_usd"].iloc[-1]
            pct_change = ((predicted_price - last_observed_price) / last_observed_price) * 100

            # Step 6: Generate recommendation based on simple threshold
            if pct_change > 5:
                recommendation = "buy"
                confidence = min(pct_change / 20, 1.0)
            elif pct_change < -5:
                recommendation = "sell"
                confidence = min(abs(pct_change) / 20, 1.0)
            else:
                recommendation = "hold"
                confidence = max(1.0 - abs(pct_change) / 5, 0.0)

            logger.info(
                f"Predicted price for product {product_id}: "
                f"${predicted_price:.2f} ({pct_change:+.2f}%)"
            )

            return {
                "predicted_price": float(predicted_price),
                "pct_change": float(pct_change),
                "recommendation": recommendation,
                "confidence": float(confidence),
            }

        except Exception as e:
            logger.error(
                f"Error predicting price for product {product_id}: {e}"
            )
            raise

    # ──────────────────────────────────────────────────────────────────────────
    # PUBLIC FUNCTION 2: ANOMALY DETECTION
    # ──────────────────────────────────────────────────────────────────────────

    def detect_anomaly(self, product_id: str, region_code: str) -> Dict:
        """
        Detect if a product's price is anomalous using autoencoder reconstruction error.

        Args:
            product_id: product ID as string (UUID)
            region_code: region code

        Returns:
            Dict with:
                - is_anomalous: bool
                - severity: str ('none', 'low', 'medium', 'high')
                - reconstruction_error: float
                - threshold_used: float
        """
        try:

            # Fetch same data as predict_single_product
            price_history = self._fetch_price_history(product_id, region_code, months=36)
            start_date = price_history["date"].min()
            end_date = price_history["date"].max()

            macro_history = self._fetch_macro_history(start_date, end_date)
            market_avg_prices = self._fetch_market_avg_prices(start_date, end_date)

            # Build feature matrix
            X = self.fe.build_input_sequence(price_history, macro_history, market_avg_prices)

            # Run through autoencoder
            with torch.no_grad():
                X_tensor = torch.tensor(X, dtype=torch.float32)
                X_reconstructed = self.models["autoencoder"](X_tensor)
                reconstruction_error = F.mse_loss(X_tensor, X_reconstructed).item()

            # Load threshold
            threshold = self.autoencoder_threshold

            # Determine severity levels
            if reconstruction_error < threshold:
                severity = "none"
                is_anomalous = False
            elif reconstruction_error < threshold * 1.5:
                severity = "low"
                is_anomalous = True
            elif reconstruction_error < threshold * 2:
                severity = "medium"
                is_anomalous = True
            else:
                severity = "high"
                is_anomalous = True

            logger.info(
                f"Anomaly check for product {product_id}: "
                f"error={reconstruction_error:.4f}, severity={severity}"
            )

            return {
                "is_anomalous": bool(is_anomalous),
                "severity": severity,
                "reconstruction_error": float(reconstruction_error),
                "threshold_used": float(threshold),
            }

        except Exception as e:
            logger.error(f"Error detecting anomaly for product {product_id}: {e}")
            raise

    # ──────────────────────────────────────────────────────────────────────────
    # PUBLIC FUNCTION 3: BATCH PREDICTION
    # ──────────────────────────────────────────────────────────────────────────

    def predict_batch(
        self, product_ids: List[str], region_code: str, store_ids: List[str] = None
    ) -> List[Dict]:
        """
        Predict prices for a batch of products.
        Used by daily scheduler to regenerate all forecasts at once.

        Args:
            product_ids: list of product IDs (as strings)
            region_code: region code
            store_ids: optional list of store IDs to filter by

        Returns:
            List of dicts, each with same format as predict_single_product()
        """
        results = []
        failed = []

        logger.info(f"Starting batch prediction for {len(product_ids)} products")

        for i, product_id in enumerate(product_ids):
            try:
                pred = self.predict_single_product(product_id, region_code, store_ids)
                pred["product_id"] = product_id
                results.append(pred)
                if (i + 1) % 10 == 0:
                    logger.info(f"Batch progress: {i + 1}/{len(product_ids)}")
            except Exception as e:
                logger.warning(f"Skipped product {product_id}: {e}")
                failed.append(product_id)

        logger.info(
            f"Batch prediction complete. "
            f"Succeeded: {len(results)}, Failed: {len(failed)}"
        )

        if failed:
            logger.warning(f"Failed product IDs: {failed}")

        return results
