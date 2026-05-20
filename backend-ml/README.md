# Price Predictor - ML-Powered Price Forecasting Server

A production-grade machine learning inference server for real-time price prediction and anomaly detection. Built with FastAPI and PyTorch, this system leverages historical pricing and macro-economic data to forecast future prices with high accuracy.

[![Python 3.9+](https://img.shields.io/badge/python-3.9+-blue.svg)](https://www.python.org/downloads/)
[![FastAPI](https://img.shields.io/badge/fastapi-0.100+-green.svg)](https://fastapi.tiangolo.com/)
[![PyTorch](https://img.shields.io/badge/pytorch-2.0+-red.svg)](https://pytorch.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🎯 Overview

The Price Predictor system combines **deep learning time-series models** with **comprehensive feature engineering** to deliver accurate 6-month price forecasts. The multi-model ensemble approach ensures robustness through diverse architectures (LSTM, CNN-LSTM, TFT, Autoencoder).

**Key Capabilities:**
- 📊 Single & batch product price predictions
- 🔍 Anomaly detection via reconstruction error
- ⚡ Real-time inference (310ms latency)
- 📈 45 engineered features from price & macro data
- 🗄️ PostgreSQL integration for historical data
- 🔐 API authentication with X-API-Key

## 🏗️ Architecture

```
Database (PostgreSQL)
    ↓
[36-month price history + FRED macro indicators]
    ↓
Feature Engineering (45 features)
    ├─ Price lags (5): 1m, 2m, 3m, 6m, 12m
    ├─ Rolling statistics (12): mean, std, min, max
    ├─ Momentum & volatility (7)
    ├─ Calendar features (5): month, quarter, seasonality
    ├─ Cross-product ratios (1)
    └─ Macro indicators (13): CPI, inflation, employment, oil
    ↓
MinMax Scaling (to [0, 1] range)
    ↓
Model Ensemble
    ├─ LSTM (2-layer, 128 hidden)         → 5.03% MAPE
    ├─ CNN-LSTM (convolutional + RNN)
    ├─ TFT (Temporal Fusion Transformer)
    └─ Autoencoder (anomaly detection)
    ↓
Post-processing & Validation
    ├─ Inverse scaling (scaled → USD)
    ├─ Percent change calculation
    ├─ Sanity checks (±50% bounds)
    └─ Recommendation generation
    ↓
API Response (JSON)
```

## 🚀 Quick Start

### Prerequisites

- Python 3.9+
- PostgreSQL 12+
- 4GB RAM minimum
- 500MB disk space for models

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/costanho/price-predictor.git
cd price-predictor
git checkout backend-ml
```

2. **Create virtual environment:**
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

3. **Install dependencies:**
```bash
pip install -r requirements.txt
```

4. **Configure environment:**
```bash
cp .env.example .env
# Edit .env with your configuration:
# - DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
# - API_KEY (change to a strong secret in production)
# - MODELS_DIR (path to model artifacts)
```

5. **Prepare model artifacts:**
```bash
# Ensure these files exist in ./models/:
# - best_lstm.pth
# - best_cnn_lstm.pth
# - best_autoencoder.pth
# - scalers.pkl
# - feature_cols.pkl
# - autoencoder_threshold.pkl
```

6. **Start the server:**
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Server will be available at `http://localhost:8000`

Health check: `curl http://localhost:8000/health`

## 📚 API Documentation

### Base URL
```
http://localhost:8000
```

### Authentication
All endpoints (except `/health`) require the `X-API-Key` header:
```bash
curl -H "X-API-Key: your-secret-key" http://localhost:8000/predict/product-123
```

### Endpoints

#### 1. Health Check
```http
GET /health
```
**No authentication required**

**Response:**
```json
{
  "status": "ok",
  "models_loaded": true,
  "models": ["lstm", "cnn_lstm", "autoencoder"]
}
```

---

#### 2. Single Product Prediction
```http
GET /predict/{product_id}?region=US&stores=store-001,store-002
```

**Parameters:**
- `product_id` (path): Product UUID
- `region` (query): Region code (default: "national")
- `stores` (query, optional): Comma-separated store IDs for filtered pricing

**Response:**
```json
{
  "product_id": "product-123",
  "region": "US",
  "current_price": 4.50,
  "predicted_price": 4.72,
  "percent_change": 4.89,
  "recommendation": "wait",
  "confidence_score": 97,
  "model_version": "lstm_v1",
  "data_source": "all_sources"
}
```

**Recommendation Logic:**
- `"buy_now"`: Price expected to drop ≥2%
- `"wait"`: Price expected to increase or drop <2%

---

#### 3. Batch Predictions
```http
GET /predict/batch?product_ids=prod1,prod2,prod3&region=US
```

**Parameters:**
- `product_ids` (query): Comma-separated product IDs
- `region` (query): Region code (default: "national")
- `stores` (query, optional): Comma-separated store IDs

**Response:**
```json
[
  {
    "product_id": "prod1",
    "region": "US",
    "current_price": 4.50,
    "predicted_price": 4.72,
    "percent_change": 4.89,
    "recommendation": "wait",
    "confidence_score": 97,
    "model_version": "lstm_v1"
  },
  ...
]
```

**Use Cases:**
- Daily batch forecasting (100+ products)
- Bulk price updates
- Scheduled refresh jobs

---

#### 4. Anomaly Detection
```http
GET /anomaly/{product_id}?region=US
```

**Parameters:**
- `product_id` (path): Product UUID
- `region` (query): Region code (default: "national")

**Response:**
```json
{
  "product_id": "product-123",
  "region": "US",
  "is_anomalous": false,
  "severity": "none",
  "reconstruction_error": 0.0234,
  "threshold_used": 0.0500
}
```

**Severity Levels:**
- `"none"`: reconstruction_error < threshold
- `"low"`: threshold < error < 1.5×threshold
- `"medium"`: 1.5×threshold < error < 2×threshold
- `"high"`: error > 2×threshold

---

### Example Usage

#### Python
```python
import requests

API_KEY = "your-secret-key"
BASE_URL = "http://localhost:8000"

# Single product prediction
response = requests.get(
    f"{BASE_URL}/predict/product-123",
    params={"region": "US"},
    headers={"X-API-Key": API_KEY}
)
prediction = response.json()
print(f"Current: ${prediction['current_price']}, Predicted: ${prediction['predicted_price']}")

# Batch predictions
response = requests.get(
    f"{BASE_URL}/predict/batch",
    params={
        "product_ids": "prod1,prod2,prod3,prod4,prod5",
        "region": "national"
    },
    headers={"X-API-Key": API_KEY}
)
predictions = response.json()
print(f"Predictions for {len(predictions)} products")

# Anomaly detection
response = requests.get(
    f"{BASE_URL}/anomaly/product-123",
    params={"region": "US"},
    headers={"X-API-Key": API_KEY}
)
anomaly = response.json()
if anomaly['is_anomalous']:
    print(f"ALERT: Anomaly detected (severity: {anomaly['severity']})")
```

#### cURL
```bash
# Health check
curl http://localhost:8000/health

# Single prediction
curl -H "X-API-Key: your-key" \
  "http://localhost:8000/predict/product-123?region=US"

# Batch predictions
curl -H "X-API-Key: your-key" \
  "http://localhost:8000/predict/batch?product_ids=p1,p2,p3&region=national"

# Anomaly detection
curl -H "X-API-Key: your-key" \
  "http://localhost:8000/anomaly/product-123?region=US"
```

## 📊 Performance Metrics

| Metric | Value | Notes |
|--------|-------|-------|
| **Latency (single)** | 310ms | P50 latency for single product |
| **Throughput** | 3.2 req/s | Single-threaded, non-batched |
| **Batch throughput** | 75 req/s | 100 products batched |
| **Success rate** | 97% | Successful predictions |
| **LSTM MAPE** | 5.03% | Mean Absolute Percentage Error |
| **Memory** | ~500MB | Models in memory (CPU) |
| **Anomaly detection** | <20ms | Autoencoder inference |

## 🏷️ Project Structure

```
price-predictor/
├── app/
│   ├── __init__.py
│   ├── main.py                 # FastAPI app, lifespan management
│   ├── routes.py               # API endpoints
│   ├── predictor.py            # Inference engine, model orchestration
│   ├── feature_engineer.py     # 45-feature extraction pipeline
│   └── model_loader.py         # Model loading (PyTorch + Darts)
│
├── models/
│   ├── best_lstm.pth           # LSTM model weights
│   ├── best_cnn_lstm.pth       # CNN-LSTM model weights
│   ├── best_autoencoder.pth    # Autoencoder weights
│   ├── best_tft_darts/         # Temporal Fusion Transformer
│   ├── scalers.pkl             # MinMax scalers from training
│   ├── feature_cols.pkl        # Feature column order
│   └── autoencoder_threshold.pkl # Anomaly detection threshold
│
├── Price_Model_Server_Documentation.ipynb
│   └── Comprehensive Jupyter notebook with visualizations
│
├── main.py                     # Entry point
├── Dockerfile                  # Container configuration
├── requirements.txt            # Python dependencies
├── .env.example                # Environment template
├── .gitignore                  # Git exclusions
└── README.md                   # This file
```

## 🔧 Configuration

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=prices_db
DB_USER=postgres
DB_PASSWORD=your_password

# API Security
API_KEY=change-this-in-production

# Model artifacts
MODELS_DIR=./models

# Logging
LOG_LEVEL=INFO
```

### Database Schema

**product_prices table:**
```sql
CREATE TABLE product_prices (
    product_id VARCHAR(255),
    region_code VARCHAR(50),
    store_id VARCHAR(255),
    date DATE,
    price_usd DECIMAL(10,4),
    PRIMARY KEY (product_id, region_code, store_id, date)
);
```

**macro_indicators table:**
```sql
CREATE TABLE macro_indicators (
    indicator_date DATE,
    indicator_code VARCHAR(20),
    value DECIMAL(12,4),
    PRIMARY KEY (indicator_date, indicator_code)
);
```

## 🧠 Feature Engineering

The system extracts **45 features** from raw price and macro data:

### Price-Based Features (29)
- **Lags**: 1m, 2m, 3m, 6m, 12m (5 features)
- **Rolling stats** (3, 6, 12 months): mean, std, min, max (12 features)
- **Momentum & volatility**: 3m/12m momentum, 12m volatility (3 features)
- **Percent changes**: 1m, 3m, 6m, 12m (4 features)
- **Cross-product ratio**: Price / Market average (1 feature)
- **Base price** (1 feature)
- **Market average price** (1 feature)

### Calendar Features (5)
- Month sine/cosine encoding
- Quarter sine/cosine encoding
- Q4 binary flag (holiday shopping)

### Macro-Economic Features (13)
- **Raw FRED indicators**: CPI, gas prices, unemployment, sentiment, oil, exchange rate (8)
- **Derived metrics**: CPI MoM, CPI YoY, gas average, gas change, oil average (5)

### Scaling
All features are scaled to [0, 1] using **MinMax scaling** with statistics from the training set. This ensures consistency between training and inference.

## 🤖 Model Ensemble

### LSTM (2-layer)
- **Architecture**: 45 inputs → 128 hidden → 6 outputs
- **Performance**: 5.03% MAPE (Mean Absolute Percentage Error)
- **Strength**: Captures long-term temporal dependencies
- **Status**: Primary model for production

### CNN-LSTM
- **Architecture**: Convolutional feature extraction + LSTM sequencing
- **Strength**: Multi-scale pattern recognition
- **Status**: Backup/ensemble model

### Temporal Fusion Transformer (TFT)
- **Architecture**: Attention-based sequence-to-sequence
- **Source**: Darts library
- **Strength**: High-capacity, interpretable attention weights
- **Status**: Optional, loaded from Darts

### Autoencoder
- **Architecture**: Encoder-Decoder with bottleneck
- **Purpose**: Anomaly detection via reconstruction error
- **Strength**: Unsupervised, detects out-of-distribution prices
- **Status**: Used for anomaly detection endpoint

## 🚨 Error Handling & Fallbacks

| Scenario | Behavior |
|----------|----------|
| **Insufficient store-specific data** | Fall back to all available data |
| **Missing macro indicators** | Forward-fill, backward-fill, then fill zeros |
| **<12 months history** | Return error with data requirement |
| **Prediction >±50% from current** | Sanity check triggers, reset to 0% change |
| **Database connection error** | 500 error with detailed logging |
| **Missing API key** | 401 Unauthorized |
| **Models not loaded** | 503 Service Unavailable |

## 📈 Monitoring & Debugging

### Logging Levels
- **DEBUG**: Feature values, scaling parameters, matrix shapes
- **INFO**: Stage completion, model loads, predictions
- **WARNING**: Missing data, anomalies, fallback triggers
- **ERROR**: Connection failures, invalid inputs, OOM

### Key Monitoring Points
1. Feature matrix shape (must be 1, 12, 45)
2. Value ranges (features should be [0, 1] after scaling)
3. NaN/Inf detection
4. Prediction bounds (±50% check)
5. Pipeline latency by stage

## 🐳 Docker Deployment

### Build image
```bash
docker build -t price-predictor:latest .
```

### Run container
```bash
docker run -d \
  --name price-predictor \
  -p 8000:8000 \
  -e DB_HOST=host.docker.internal \
  -e API_KEY=your-secret-key \
  price-predictor:latest
```

## 🔄 Continuous Improvement

### Planned Enhancements
- [ ] Quarterly model retraining with new data
- [ ] SHAP explainability for feature importance
- [ ] A/B testing framework (LSTM vs TFT)
- [ ] GPU support for inference
- [ ] Redis caching layer
- [ ] Prometheus metrics export
- [ ] Grafana dashboard
- [ ] Model quantization (3-4x faster inference)

## 📖 Documentation

Comprehensive Jupyter notebook included: `Price_Model_Server_Documentation.ipynb`

Covers:
- Architecture overview
- Feature engineering pipeline with visualizations
- ML algorithm specifications
- Performance benchmarks
- Debugging strategies
- Optimization recommendations

## 🧪 Testing

### Health check
```bash
curl http://localhost:8000/health
```

### Feature engineering validation
```bash
# In Python
python -c "from app.feature_engineer import load_feature_engineer; fe = load_feature_engineer('./models'); print(f'Features: {fe.n_features}')"
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit changes (`git commit -am 'Add feature'`)
4. Push to branch (`git push origin feature/your-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see LICENSE file for details.

## 👤 Contact & Support

**Author**: Costan Haringo  
**Email**: costanharingo@gmail.com  
**Repository**: https://github.com/costanho/price-predictor

### Support
For issues, questions, or suggestions:
1. Check the documentation notebook
2. Review error logs with `LOG_LEVEL=DEBUG`
3. Open a GitHub issue with reproduction steps

---

**Last Updated**: May 2026  
**Version**: 1.0.0 (backend-ml branch)
