# Price Predictor

A full-stack price monitoring and forecasting application with real-time store price comparison and ML-powered price prediction.

## 📁 Project Structure

```
price-predictor/
├── frontend/              # React/Next.js frontend (localhost:3000)
├── backend-api/           # Spring Boot REST API (localhost:8080)
├── backend-ml/            # Python ML model server (localhost:8001)
├── docker-compose.yml     # Multi-container orchestration
└── README.md
```

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Git

### Option 1: Docker Compose (Recommended)
```bash
cd price-predictor
docker-compose up --build
```

Access:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- ML Server: http://localhost:8001
- Database: localhost:5432

### Option 2: Local Development

#### Backend API (Spring Boot)
```bash
cd backend-api
mvn spring-boot:run
```

#### ML Server (Python)
```bash
cd backend-ml
pip install -r requirements.txt
python app.py
```

#### Frontend (React)
```bash
cd frontend
npm install
npm start
```

## 🏗️ Architecture

### Backend API (Spring Boot 4.0.6)
- JWT authentication (HS512)
- REST endpoints for forecasts, products, alerts, carts
- PostgreSQL persistence
- External API integrations (Kroger, Spoonacular)
- Real-time alerts with soft delete and expiration

### ML Server (Python)
- Price forecasting model
- Regional price predictions
- Store price factor calculations
- 7-day cache TTL for predictions

### Frontend (React)
- Product search and filtering
- Price comparison by store
- Personalized alerts
- Cart management
- Regional price trends

## 📊 Key Features

- **Store Price Comparison**: Real-time pricing from Walmart, Kroger, Aldi, Target, Whole Foods, Trader Joe's
- **ML Price Forecasting**: Predict future prices based on historical trends
- **Smart Alerts**: Automatic notifications for price drops, target price hits, and anomalies
- **Regional Analysis**: Price comparisons across US regions (Northeast, Midwest, South, West)
- **User Tracking**: Personalized store preferences and price alerts
- **Shield Score**: Product price safety rating

## 🔧 Configuration

Create `.env` file in root:
```
POSTGRES_PASSWORD=mypassword
MODEL_SERVER_URL=http://backend-ml:8001
API_KEY=your-api-key-here
KROGER_CLIENT_ID=your-kroger-id
KROGER_CLIENT_SECRET=your-kroger-secret
```

## 📡 API Endpoints

### Forecasts
- `GET /forecasts/{productId}?region=northeast` - Get forecast with store prices
- `GET /forecasts/{productId}/comparison?region=northeast` - Detailed store comparison
- `GET /forecasts/summary?region=northeast&page=1&pageSize=50` - Paginated forecast list

### Products
- `GET /products` - List all products
- `GET /products/{id}` - Get product details
- `GET /products/{id}/shield-score` - Product price safety score

### Alerts
- `GET /alerts?unreadOnly=true&limit=50` - User's alerts
- `PATCH /alerts/{id}/read` - Mark alert as read
- `DELETE /alerts/{id}` - Delete alert

### Authentication
- `POST /auth/register` - Create user account
- `POST /auth/login` - Get JWT token
- `POST /auth/validate` - Validate token

## 🗄️ Database

PostgreSQL with 17 entity tables:
- Users, Products, PriceHistories, PriceForecasts
- Alerts, UserTrackedStores, UserProductTracking
- Cart, CartItems, StorePriceSyncLog, ProductNameMappings
- And more...

## 🧪 Testing

```bash
# Backend tests
cd backend-api
mvn test

# API testing
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/forecasts/summary?region=northeast
```

## 📝 Development

### Making Changes
1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes and test locally
3. Push and create pull request

### Deployment
```bash
git push origin main
# GitHub Actions will build and deploy
```

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Open pull request

## 📄 License

MIT License

## 📧 Contact

For issues and questions, open a GitHub issue.
