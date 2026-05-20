# Price Monitor API - Testing Guide

## Prerequisites

Before testing, ensure:
1. ✅ PostgreSQL running on localhost:5432 with `ai_price_monitor` database
2. ✅ Python model server running on port 8001
3. ✅ Spring Boot application running on port 8080

## Starting the Services

### 1. Start PostgreSQL
```bash
brew services start postgresql@15
# OR
psql -U cosy -d ai_price_monitor
```

### 2. Start Python Model Server
```bash
cd /path/to/python/model/server
uvicorn app.main:app --port 8001 --reload
```

### 3. Start Spring Boot
```bash
cd /Users/cosy/Documents/price-monitor-api
mvn spring-boot:run

# OR run from IDE: right-click PriceMonitorApplication.java → Run
```

Check logs should show:
```
Started PriceMonitorApplication in X seconds
```

---

## Test Sequence

### 1. Health Check
```bash
curl http://localhost:8080/products
# Expected: 200 OK, empty array []
```

### 2. User Registration
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePassword123!",
    "name": "Test User",
    "zipCode": "10001"
  }'

# Expected Response:
# {
#   "userId": "uuid-here",
#   "token": "eyJhbGciOiJIUzUxMiJ9...",
#   "email": "test@example.com"
# }
```

Save the `token` for subsequent requests.

### 3. User Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePassword123!"
  }'

# Expected: Same response as registration with new token
```

### 4. Get Products (Public - No Auth)
```bash
curl http://localhost:8080/products

# Expected: 200 OK, empty array (no products seeded yet)
# Response: []
```

### 5. Get Product by ID
```bash
curl http://localhost:8080/products/550e8400-e29b-41d4-a716-446655440000

# Expected: 404 (product doesn't exist)
```

### 6. Search Products
```bash
curl "http://localhost:8080/products/search?q=eggs"

# Expected: 200 OK, empty array []
```

### 7. Get Forecast (Protected - Requires JWT)
Replace `{token}` with actual JWT from registration:

```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

curl -X GET "http://localhost:8080/forecasts/550e8400-e29b-41d4-a716-446655440000?region=national" \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with forecast data
# Or: Error if Python server not running
```

### 8. Get User Profile (Protected)
```bash
curl -X GET "http://localhost:8080/user/profile" \
  -H "Authorization: Bearer $TOKEN"

# Expected Response:
# {
#   "id": "uuid",
#   "email": "test@example.com",
#   "name": "Test User",
#   "zipCode": "10001",
#   "region": "Northeast",
#   "notifications": {
#     "priceDrops": true,
#     "anomalies": true,
#     "dealDna": true,
#     "forecastUpdates": true,
#     "weeklyEmail": true
#   }
# }
```

### 9. Update User Profile (Protected)
```bash
curl -X PUT "http://localhost:8080/user/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "zipCode": "20002"
  }'

# Expected: { "success": true, "message": "Profile updated successfully" }
```

### 10. Get Cart (Protected)
```bash
curl -X GET "http://localhost:8080/cart" \
  -H "Authorization: Bearer $TOKEN"

# Expected: { "items": [], "totalItems": 0 }
```

### 11. Add to Cart (Protected)
```bash
curl -X POST "http://localhost:8080/cart/add" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2
  }'

# Expected: { "success": true, "message": "Item added to cart" }
# Or error if product doesn't exist
```

### 12. Get Alerts (Protected)
```bash
curl -X GET "http://localhost:8080/alerts?unreadOnly=false" \
  -H "Authorization: Bearer $TOKEN"

# Expected: { "alerts": [], "unreadCount": 0 }
```

### 13. Test Protected Endpoint Without Auth
```bash
curl -X GET "http://localhost:8080/cart" \
  -H "Content-Type: application/json"

# Expected: 401 Unauthorized
```

### 14. Test Invalid Token
```bash
curl -X GET "http://localhost:8080/cart" \
  -H "Authorization: Bearer invalid-token-xyz"

# Expected: 401 Unauthorized with error message
```

---

## Troubleshooting

### Spring Boot won't start
**Error**: `Connection refused` to PostgreSQL
- **Fix**: Ensure PostgreSQL is running: `brew services start postgresql@15`

**Error**: `Port 8080 already in use`
- **Fix**: Kill the existing process: `lsof -i :8080` → `kill -9 <PID>`

### Forecast endpoint returns error
**Error**: `Failed to connect to model server`
- **Fix**: 
  1. Verify Python server is running: `curl http://localhost:8001/docs`
  2. Check `model.server.url` in `application.properties` is `http://localhost:8001`
  3. Check Python server logs for errors

### JWT token invalid
**Error**: `Invalid token` when using Bearer token
- **Fix**: 
  1. Verify token is not expired (24 hour expiration)
  2. Get a fresh token from `/auth/login`
  3. Ensure token is copied completely without extra spaces

### Database connection fails
**Error**: `role "cosy" does not exist`
- **Fix**: Connect to PostgreSQL as a valid user:
  ```bash
  psql -U postgres
  CREATE ROLE cosy WITH LOGIN PASSWORD 'password';
  ALTER ROLE cosy CREATEDB;
  ```

---

## Database Verification

Check what's in the database:
```bash
psql -U cosy -d ai_price_monitor

# List tables:
\dt

# Check users table:
SELECT * FROM users;

# Check if JWT is working (you should see created user):
SELECT email, is_active, created_at FROM users;
```

---

## API Summary

| Method | Endpoint | Auth | Status |
|--------|----------|------|--------|
| POST | /auth/register | ❌ | ✅ Ready |
| POST | /auth/login | ❌ | ✅ Ready |
| POST | /auth/validate | ❌ | ✅ Ready |
| GET | /products | ❌ | ✅ Ready |
| GET | /products/{id} | ❌ | ✅ Ready |
| GET | /products/search?q=... | ❌ | ✅ Ready |
| GET | /forecasts/{id}?region=... | ✅ | ✅ Ready |
| GET | /cart | ✅ | ✅ Ready |
| POST | /cart/add | ✅ | ✅ Ready |
| DELETE | /cart/{id} | ✅ | ✅ Ready |
| POST | /cart/optimize | ✅ | ✅ Ready |
| GET | /alerts | ✅ | ✅ Ready |
| PATCH | /alerts/{id}/read | ✅ | ✅ Ready |
| GET | /user/profile | ✅ | ✅ Ready |
| PUT | /user/profile | ✅ | ✅ Ready |

---

## Next Steps

1. Test basic endpoints (no auth) first
2. Register a user and get JWT token
3. Test protected endpoints with JWT
4. Load sample product data into database
5. Test forecast endpoint with real products
6. Load store price factors for cart optimization
7. Test end-to-end workflows

