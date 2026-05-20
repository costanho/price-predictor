# ShopsPricePredictor — Frontend

Cross-platform frontend built with **Expo + React Native**, running on both **Web** and **Mobile** from a single codebase. It connects to the Spring Boot backend API for price predictions, cart optimization, and alerts.

---

## Prerequisites

- [Node.js](https://nodejs.org/) v18 or higher
- [npm](https://www.npmjs.com/) v9 or higher
- [Expo CLI](https://docs.expo.dev/get-started/installation/) (optional, installed via npx)

---

## Installation

```bash
cd frontend
npm install
```

---

## Environment Variables

Create a `.env.local` file in the `frontend/` folder:

```env
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080
```

| Variable | Description | Default |
|---|---|---|
| `EXPO_PUBLIC_API_BASE_URL` | URL of the Spring Boot backend API | `http://localhost:8080` |

> For production, replace `localhost:8080` with your deployed API URL.

---

## Running the App

### Web (Browser)
```bash
npx expo start --web
```
Opens at `http://localhost:8081`

### Mobile (Expo Go)
```bash
npx expo start
```
Scan the QR code with the [Expo Go](https://expo.dev/go) app on your phone.

### Android Emulator
```bash
npm run android
```

### iOS Simulator (Mac only)
```bash
npm run ios
```

---

## Project Structure

```
frontend/
├── api/                  # API client & service calls
│   ├── client.ts         # Axios instance with JWT interceptors
│   ├── auth.ts           # Login & register
│   ├── products.ts       # Product listing & price history
│   ├── forecasts.ts      # Price predictions
│   ├── cart.ts           # Cart management & optimization
│   └── alerts.ts         # Price drop alerts
│
├── app/
│   └── index.tsx         # Entry point — renders Web or Mobile dashboard
│
├── components/
│   └── ui/               # Shared UI components (alerts, compare, profile, etc.)
│
├── hooks/                # Custom React hooks (useProducts, useCart, etc.)
├── store/                # Zustand global state (useStore.ts)
├── mobile/               # Mobile-specific dashboard & navigation
├── web/                  # Web-specific dashboard & sidebar
├── utils/
│   └── storage.ts        # Secure token storage (expo-secure-store)
├── assets/               # Images & fonts
├── app.json              # Expo app config
├── package.json
└── tsconfig.json
```

---

## How Authentication Works

1. User logs in via `api/auth.ts`
2. JWT token is saved securely using `expo-secure-store`
3. All API requests automatically attach the token via an Axios interceptor in `api/client.ts`
4. On 401 responses, the token is cleared and the user is logged out

---

## Connecting to the Backend

Make sure the **Spring Boot API** is running before starting the frontend. By default it runs on port `8080`.

```bash
# From the backend-api folder
mvn spring-boot:run
```

Update `EXPO_PUBLIC_API_BASE_URL` in `.env.local` if your API runs on a different host or port.

---

## Tech Stack

| Tool | Purpose |
|---|---|
| Expo + React Native | Cross-platform framework |
| React Navigation | Screen navigation |
| Axios | HTTP requests |
| Zustand | Global state management |
| expo-secure-store | Secure JWT storage |
| TypeScript | Type safety |
