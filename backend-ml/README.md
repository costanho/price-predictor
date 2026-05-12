# Backend ML Server

Python ML model server for price forecasting.

## Setup

```bash
pip install -r requirements.txt
python app.py
```

## Endpoints

- `GET /predict/{product_id}?region={region}&stores={stores}` - Get price forecast
- `POST /train` - Retrain forecasting model
- `GET /health` - Health check

## Environment Variables

```
DATABASE_URL=postgresql://postgres:mypassword@localhost:5432/price_monitor
FLASK_ENV=production
PORT=8001
```

## Model Details

- Algorithm: ARIMA + Store price factors
- Training data: 24 months of historical prices
- Update frequency: Daily
- Cache TTL: 7 days
