# Price Predictor 📊

AI-powered price forecasting system with machine learning backend, REST API, and web dashboard.

## Architecture

```
price-predictor/
├── frontend/          # React/Next.js UI dashboard
├── backend-api/       # Spring Boot REST API (price forecasting service)
├── backend-ml/        # Python LSTM inference server (ML predictions)
└── docker-compose.yml # Orchestration for all services
```

## Services

### Frontend
- **Tech:** React/Next.js
- **Purpose:** UI for viewing price forecasts and historical trends
- **Port:** 3000

### Backend API
- **Tech:** Spring Boot (Java)
- **Purpose:** REST API for forecasting requests, caching, authentication
- **Port:** 8080
- **Features:**
  - X-API-Key authentication
  - Price forecast caching
  - Store-based filtering

### Backend ML
- **Tech:** Python FastAPI with PyTorch LSTM
- **Purpose:** ML model inference for price predictions
- **Port:** 8001
- **Models:** LSTM, CNN-LSTM, Autoencoder

## Quick Start

```bash
docker-compose up
```

## Environment Variables

See `.env.example` for required configuration.

## API Integration

**Predict endpoint:**
```
GET /forecasts/{productId}?region=northeast&stores=kroger-ne,target-ne
Header: X-API-Key: your-api-key
```

**Response:**
```json
{
  "product_id": "003d5f04-...",
  "current_price": 112.73,
  "predicted_price": 98.50,
  "percent_change": -12.58,
  "recommendation": "buy_now",
  "data_source": "store_specific"
}
```

## Database

PostgreSQL with tables:
- `product_prices` - Historical daily prices by product/region/store
- `macro_indicators` - Economic indicators (CPI, unemployment, oil, etc.)
- `price_forecasts` - Cached predictions (24-hour TTL)

## Development

### ML Server (Python)
```bash
cd backend-ml
pip install -r requirements.txt
python -m uvicorn app.main:app --reload
```

### API Server (Spring Boot)
```bash
cd backend-api
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## License

MIT
