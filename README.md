# ShopsPricePredictor

> A cross-platform grocery price prediction platform that forecasts retail prices up to 6 months ahead using ensemble ML models, helping shoppers save money across multiple stores.

![Tech Stack](https://img.shields.io/badge/stack-Expo%20%7C%20Spring%20Boot%20%7C%20FastAPI-blue)
![Prediction Accuracy](https://img.shields.io/badge/MAPE-%7E3%25-brightgreen)
![License](https://img.shields.io/badge/license-MIT-green)

---

## What It Does

- Predicts grocery prices 6 months in advance using ARIMA, LSTM, CNN-LSTM, and Temporal Fusion Transformer models
- Compares prices across multiple stores in real time
- Optimizes your shopping cart to minimize spend
- Sends price drop alerts and anomaly notifications
- Works on both Web and Mobile from a single frontend codebase

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                        Client                           │
│         Expo Web (browser) / Expo Mobile (app)          │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP / REST
┌───────────────────────▼─────────────────────────────────┐
│                  backend-api (Spring Boot)               │
│         JWT Auth · Products · Forecasts · Alerts        │
└──────────┬────────────────────────────┬─────────────────┘
           │ PostgreSQL                 │ HTTP
┌──────────▼──────────┐   ┌────────────▼────────────────┐
│     PostgreSQL DB   │   │    backend-ml (FastAPI)      │
│  prices · users     │   │  ARIMA · LSTM · CNN-LSTM     │
│  forecasts · alerts │   │  TFT · Ensemble (~3% MAPE)   │
└─────────────────────┘   └─────────────────────────────┘
```

---

## Project Structure

```
price-predictor/
├── frontend/              # Expo + React Native (Web & Mobile)
│   ├── api/               # Axios API client & service calls
│   ├── app/               # App entry point
│   ├── components/        # Shared UI components
│   ├── hooks/             # Custom React hooks
│   ├── mobile/            # Mobile dashboard & navigation
│   ├── store/             # Zustand global state
│   ├── utils/             # Secure storage helpers
│   └── web/               # Web dashboard & sidebar
│
├── backend-api/           # Spring Boot REST API
│   ├── src/main/java/     # Java source code
│   ├── pom.xml
│   └── application.yml
│
├── backend-ml/            # FastAPI ML prediction server
│   ├── app/               # FastAPI app & routes
│   ├── models/            # Trained model weights
│   └── requirements.txt
│
├── docker-compose.yml     # Runs all services together
└── schema.sql             # PostgreSQL database schema
```

---

## Quick Start (Docker)

The fastest way to run everything:

```bash
git clone https://github.com/costanho/price-predictor.git
cd price-predictor
docker-compose up --build
```

Then open:

| Service | URL |
|---|---|
| Frontend (Web) | http://localhost:3000 |
| Backend API Docs | http://localhost:8080/swagger-ui.html |
| ML Server Docs | http://localhost:8000/docs |

---

## Local Development

### Prerequisites

| Tool | Version |
|---|---|
| Node.js | 18+ |
| Java | 17+ |
| Python | 3.10+ |
| Docker & Docker Compose | Latest |

---

### 1. Frontend (Expo + React Native)

```bash
cd frontend
npm install
```

Create a `.env.local` file inside `frontend/`:

```env
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080
```

Run on web:
```bash
npx expo start --web
```

Run on mobile (scan QR with Expo Go app):
```bash
npx expo start
```

---

### 2. Backend API (Spring Boot)

```bash
cd backend-api
mvn install
mvn spring-boot:run
```

Configure `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shopspricepredictor
    username: your_db_user
    password: your_db_password
  jpa:
    hibernate:
      ddl-auto: update
```

API runs at: http://localhost:8080

---

### 3. ML Server (FastAPI)

```bash
cd backend-ml
python -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Create a `.env` file inside `backend-ml/`:

```env
MODEL_PATH=/models/ensemble.pkl
BATCH_SIZE=32
```

ML server runs at: http://localhost:8000

---

### 4. Database Setup

```bash
psql -U your_db_user -d shopspricepredictor -f schema.sql
```

Key tables:

| Table | Purpose |
|---|---|
| `users` | User accounts & preferences |
| `products` | Product catalog |
| `prices` | Historical price data per store |
| `forecasts` | AI-generated price predictions |
| `alerts` | Price drop & anomaly notifications |
| `price_history` | Price change tracking |

---

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login, returns JWT token |
| POST | `/api/auth/register` | Create new account |

### Products
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}/history` | Price history for a product |

### Forecasts
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/forecasts/{productId}` | 6-month price prediction |
| GET | `/api/forecasts/batch` | Predictions for multiple products |

### Cart
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | User's shopping cart |
| POST | `/api/cart/optimize` | Optimize cart across stores |

### Alerts
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/alerts` | User's alerts |
| PATCH | `/api/alerts/{id}/read` | Mark alert as read |
| DELETE | `/api/alerts/{id}` | Dismiss alert |

---

## ML Models

| Model | Purpose |
|---|---|
| ARIMA | Seasonal & trend forecasting |
| LSTM | Long-term pattern recognition |
| CNN-LSTM | Complex price relationship detection |
| Temporal Fusion Transformer | Multivariate time series |
| Ensemble | Combines all models (~3% MAPE) |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Expo, React Native, Zustand, Axios |
| Backend API | Spring Boot, PostgreSQL, JWT |
| ML Server | FastAPI, TensorFlow, Scikit-learn |
| Deployment | Docker, Docker Compose |

---

## Testing

```bash
# Frontend
cd frontend && npm test

# Backend API
cd backend-api && mvn test

# ML Server
cd backend-ml && pytest
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to your branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

## Contact

**Costa Nharingo** — [@costanho](https://github.com/costanho)

Project: [https://github.com/costanho/price-predictor](https://github.com/costanho/price-predictor)
