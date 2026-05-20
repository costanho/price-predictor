# Shop Retail Price Predictor

Multi-service platform predicting retail prices using ARIMA, LSTM, CNN-LSTM, TFT, and ensemble methods (~3% MAPE).

## Quick Start

```bash
docker-compose up --build
```

Then open:
- **Frontend:** http://localhost:3000
- **API Docs:** http://localhost:8080/swagger-ui.html
- **ML Docs:** http://localhost:8000/docs

## Architecture

- **Frontend:** React dashboard for price predictions
- **Backend API:** Spring Boot REST services + PostgreSQL
- **ML Server:** FastAPI with trained ensemble models
- **Database:** PostgreSQL for historical data + predictions

## Development

```bash
# Frontend
cd frontend && npm install && npm run dev

# Spring Boot
cd backend-api && mvn spring-boot:run

# Python ML
cd backend-ml && python -m venv venv && source venv/bin/activate && pip install -r requirements.txt && uvicorn app.main:app --reload
```

## Features

### Frontend (Expo + React)
- Cross-platform web & mobile dashboards
- Real-time price forecasts (6-month outlook)
- Cart optimizer across multiple stores
- Price drop alerts & notifications
- Deal DNA pattern recognition
- Inflation shield score tracking

### Backend API (Spring Boot)
- REST API with JWT authentication
- PostgreSQL database for price history
- Price sync & forecasting service
- Alert generation & management
- Multi-store pricing aggregation

### ML Pipeline (FastAPI)
- ARIMA for seasonal trends
- LSTM for long-term patterns
- CNN-LSTM for complex relationships
- Temporal Fusion Transformer for multivariate
- Ensemble voting for ~3% MAPE accuracy

## Project Structure

```
price-predictor/
├── frontend/              # Expo + React (Web & Mobile)
│   ├── api/              # Shared API client
│   ├── hooks/            # Custom React hooks
│   ├── store/            # Zustand state management
│   ├── web/              # Web dashboard
│   ├── mobile/           # Mobile dashboard
│   └── components/       # Shared UI components
│
├── backend-api/          # Spring Boot REST API
│   ├── src/main/java
│   ├── pom.xml
│   └── application.yml
│
├── backend-ml/           # FastAPI ML Server
│   ├── models/           # Trained model weights
│   ├── app/
│   ├── requirements.txt
│   └── main.py
│
└── docker-compose.yml    # Services orchestration
```

## Database Schema

Key tables:
- `users` - User accounts & preferences
- `products` - Product catalog
- `prices` - Historical price data per store
- `forecasts` - AI-generated price predictions
- `alerts` - Price drop & anomaly notifications
- `price_history` - Price change tracking

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login with JWT
- `POST /api/auth/register` - Create account

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}/history` - Price history

### Forecasts
- `GET /api/forecasts/{productId}` - 6-month prediction
- `GET /api/forecasts/batch` - Multiple products

### Cart
- `GET /api/cart` - User's shopping cart
- `POST /api/cart/optimize` - Store-wise optimization

### Alerts
- `GET /api/alerts` - User's alerts
- `PATCH /api/alerts/{id}/read` - Mark as read
- `DELETE /api/alerts/{id}` - Dismiss alert

## Environment Variables

### Frontend (.env)
```
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080
```

### Backend API (application.yml)
```
spring.datasource.url=jdbc:postgresql://db:5432/shopspricepredictor
spring.jpa.hibernate.ddl-auto=update
```

### ML Server (.env)
```
MODEL_PATH=/models/ensemble.pkl
BATCH_SIZE=32
```

## Performance Metrics

- **Price Prediction Accuracy:** ~3% MAPE
- **Forecast Horizon:** Up to 6 months
- **Data Points:** 100K+ historical prices
- **Model Training:** Daily incremental updates
- **Response Time:** <200ms for API calls

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Expo, React, Zustand, Axios |
| API | Spring Boot, PostgreSQL, JWT |
| ML | FastAPI, TensorFlow, Scikit-learn |
| Deployment | Docker, Docker Compose |

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for local frontend dev)
- Java 17+ (for local API dev)
- Python 3.10+ (for local ML dev)

### Installation

1. Clone the repository
```bash
git clone https://github.com/costanho/price-predictor.git
cd price-predictor
```

2. Start all services
```bash
docker-compose up --build
```

3. Access the applications
- Frontend: http://localhost:3000
- API Swagger: http://localhost:8080/swagger-ui.html
- ML Docs: http://localhost:8000/docs

### Local Development

**Frontend:**
```bash
cd frontend
npm install
npm start
```

**Backend API:**
```bash
cd backend-api
mvn install
mvn spring-boot:run
```

**ML Server:**
```bash
cd backend-ml
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## Testing

```bash
# Frontend tests
cd frontend && npm test

# Backend tests
cd backend-api && mvn test

# ML tests
cd backend-ml && pytest
```

## Contributing

1. Create a feature branch (`git checkout -b feature/amazing-feature`)
2. Commit changes (`git commit -m 'feat: add amazing feature'`)
3. Push to branch (`git push origin feature/amazing-feature`)
4. Open a Pull Request

## License

MIT License - see LICENSE file for details

## Contact

Costa Nharingo - [@costanho](https://github.com/costanho)

Project Link: [https://github.com/costanho/price-predictor](https://github.com/costanho/price-predictor)
