import logging
import os
from typing import List
from fastapi import APIRouter, Query, HTTPException, status, Header
from pydantic import BaseModel, Field
from . import main

logger = logging.getLogger(__name__)

router = APIRouter()

API_KEY = os.getenv("API_KEY", "your-secret-key-change-this-in-production")


def verify_api_key(x_api_key: str = Header(None)):
    """Verify X-API-Key header for protected endpoints."""
    if not x_api_key or x_api_key != API_KEY:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing API key"
        )


# ──────────────────────────────────────────────────────────────────────────
# RESPONSE MODELS
# ──────────────────────────────────────────────────────────────────────────

class HealthResponse(BaseModel):
    status: str
    models_loaded: bool
    models: List[str]


class PredictionResponse(BaseModel):
    product_id: str
    region: str
    current_price: float
    predicted_price: float
    percent_change: float
    recommendation: str
    confidence_score: int
    model_version: str


class AnomalyResponse(BaseModel):
    product_id: str
    region: str
    is_anomalous: bool
    severity: str
    reconstruction_error: float
    threshold_used: float


# ──────────────────────────────────────────────────────────────────────────
# ENDPOINT 1: Health Check
# ──────────────────────────────────────────────────────────────────────────

@router.get("/health", response_model=HealthResponse)
async def health():
    """Health check. No authentication required."""
    try:
        models_loaded = main.predictor is not None and main.models is not None
        return HealthResponse(
            status="ok",
            models_loaded=models_loaded,
            models=["lstm", "cnn_lstm", "autoencoder"]
        )
    except Exception as e:
        logger.error(f"Health check error: {e}")
        return HealthResponse(
            status="error",
            models_loaded=False,
            models=[]
        )


# ──────────────────────────────────────────────────────────────────────────
# ENDPOINT 2: Batch Predictions (MUST be before single product endpoint)
# ──────────────────────────────────────────────────────────────────────────

@router.get("/predict/batch", response_model=List[PredictionResponse])
async def predict_batch(
    product_ids: str = Query(..., description="Comma-separated product IDs"),
    region: str = Query("national", description="Region code"),
    stores: str = Query(None, description="Comma-separated store IDs (optional)"),
    x_api_key: str = Header(None)
):
    """Get price predictions for multiple products."""
    verify_api_key(x_api_key)

    try:
        if main.predictor is None:
            raise HTTPException(status_code=503, detail="Predictor not initialized")

        ids = [pid.strip() for pid in product_ids.split(",")]
        store_ids = [s.strip() for s in stores.split(",")] if stores else None

        results = main.predictor.predict_batch(ids, region, store_ids)

        return [
            PredictionResponse(
                product_id=r.get("product_id"),
                region=region,
                current_price=r["current_price"],
                predicted_price=r["predicted_price"],
                percent_change=r["percent_change"],
                recommendation=r["recommendation"],
                confidence_score=r["confidence_score"],
                model_version="lstm_v1"
            )
            for r in results
        ]
    except Exception as e:
        logger.error(f"Batch prediction error: {e}")
        raise HTTPException(status_code=500, detail="Batch prediction failed")


# ──────────────────────────────────────────────────────────────────────────
# ENDPOINT 3: Single Product Prediction
# ──────────────────────────────────────────────────────────────────────────

@router.get("/predict/{product_id}", response_model=PredictionResponse)
async def predict(
    product_id: str,
    region: str = Query("national", description="Region code"),
    stores: str = Query(None, description="Comma-separated store IDs (optional)"),
    x_api_key: str = Header(None)
):
    """Get price prediction for a single product."""
    verify_api_key(x_api_key)

    try:
        if main.predictor is None:
            raise HTTPException(status_code=503, detail="Predictor not initialized")

        # Parse store_ids if provided
        store_ids = [s.strip() for s in stores.split(",")] if stores else None

        result = main.predictor.predict_single_product(product_id, region, store_ids)
        return PredictionResponse(
            product_id=product_id,
            region=region,
            current_price=result["current_price"],
            predicted_price=result["predicted_price"],
            percent_change=result["percent_change"],
            recommendation=result["recommendation"],
            confidence_score=result["confidence_score"],
            model_version="lstm_v1"
        )
    except Exception as e:
        logger.error(f"Prediction error for {product_id}: {e}")
        raise HTTPException(status_code=500, detail="Prediction failed")


# ──────────────────────────────────────────────────────────────────────────
# ENDPOINT 4: Anomaly Detection
# ──────────────────────────────────────────────────────────────────────────

@router.get("/anomaly/{product_id}", response_model=AnomalyResponse)
async def anomaly(
    product_id: str,
    region: str = Query("national", description="Region code"),
    x_api_key: str = Header(None)
):
    """Detect anomalous prices for a product."""
    verify_api_key(x_api_key)

    try:
        if main.predictor is None:
            raise HTTPException(status_code=503, detail="Predictor not initialized")

        result = main.predictor.detect_anomaly(product_id, region)
        return AnomalyResponse(
            product_id=product_id,
            region=region,
            is_anomalous=result["is_anomalous"],
            severity=result["severity"],
            reconstruction_error=result["reconstruction_error"],
            threshold_used=result["threshold_used"]
        )
    except Exception as e:
        logger.error(f"Anomaly detection error for {product_id}: {e}")
        raise HTTPException(status_code=500, detail="Anomaly detection failed")
