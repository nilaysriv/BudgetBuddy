# BudgetBuddy

A personal budgeting app for Android — track income and expenses, set monthly budgets per category, and see where your money goes. Built with Jetpack Compose and a small Node/Express + Postgres backend so multiple people can each have their own account and data.

## Features

- Email/password accounts (full name, email, password) — each user's transactions, categories, and budgets are private to them
- Local-first: works offline via Room, syncs to the backend when reachable
- Dashboard with balance, income/expense summary, a 7-day trend chart, top spending categories, budget progress, and recent transactions
- Categories and budgets management
- Per-user currency (INR by default; USD/EUR/GBP also supported) from Settings
- Dark mode
- Adaptive layout — usable on both phone and tablet screens

## Tech stack

- **App**: Kotlin, Jetpack Compose (Material 3), Hilt, Room, Retrofit, Navigation 3
- **Backend**: Node.js, Express, PostgreSQL, JWT auth
- **Infra**: Docker Compose (Postgres + backend), GitHub Actions for release builds

## Getting started

### Backend

```bash
cp .env.example .env   # fill in POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD, JWT_SECRET
docker compose up -d --build
```

This starts Postgres (applying `backend/init.sql`) and the Express API on port `3434`.

### Android app

1. Point the app at your backend by editing `app/src/main/assets/env`:
   ```
   BACKEND_URL=http://<your-backend-host>:3434
   ```
   Use `http://10.0.2.2:3434` to reach a backend running on your host machine from the Android emulator.
2. Open the project in Android Studio and run the `app` module, or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```

### Releases

Pushing a tag like `v1.0.0` triggers `.github/workflows/release.yml`, which builds a debug APK and publishes it as a GitHub Release.
