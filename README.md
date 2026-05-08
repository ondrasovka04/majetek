# Majetek – Asset Management System

A full-stack asset management system with a web application, REST API, and a mobile application for offline inventory auditing. Bachelor's thesis at FIT CTU in Prague.

## Tech Stack

| Component  | Technologies                                |
| ---------- | ------------------------------------------- |
| Backend    | Java 21, Spring Boot 3.4, Maven, PostgreSQL |
| Frontend   | React 19, TypeScript 6, Vite 8, MUI 9       |
| Android    | Java 21, Gradle, Room 2.8, OkHttp 5.3       |
| Deployment | Nginx, Ubuntu VPS, GitHub Actions           |

## Prerequisites

- **Java 21** (JDK)
- **Node.js 20+** and npm
- **PostgreSQL 15+**
- **Nginx** (reverse proxy for local development)
- **Android SDK 36** (for mobile app only)

## Running the Backend

```bash
cd backend

# Create database
psql -U postgres -c "CREATE DATABASE majetek;"

# Create .env file with required configuration
cat > .env << 'EOF'
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your-min-32-characters-long-secret-key-here-change-this
JWT_REFRESH_SECRET=your-min-32-characters-long-refresh-key-here-change-this
JWT_REFRESH_COOKIE_SECURE=false
EXPORT_PATH=/tmp/majetek/exports
ATTACHMENTS_FOLDER=/tmp/majetek/attachments
ATTACHMENT_MAX_FILE_SIZE=50MB
MAX_LABELS_PER_PRINT=50
ZEBRA_APIKEY=
ZEBRA_TENANT=
ZEBRA_SN=
EOF

# Build and run
mvn clean package -DskipTests
java -jar target/majetek-*.jar
```

The backend runs at `http://localhost:8080`.

## Running the Frontend

```bash
cd frontend

# Set backend URL
echo "VITE_API_URL=/api" > .env

# Install and start
npm install
npm run dev
```

The frontend dev server runs at `http://localhost:3000`.

## Nginx (Local Development)

Both frontend and backend must be served through Nginx on the same domain to avoid CORS issues. Example configuration:

```nginx
server {
    listen 80;
    server_name localhost;

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

Then access the application at `http://localhost`.

## Building the Mobile App

```bash
cd android

# Set SDK path
echo "sdk.dir=/path/to/Android/sdk" > local.properties

# Build APK
./gradlew assembleDebug
```

The APK is located at `android/app/build/outputs/apk/debug/`.

## Running Tests

```bash
# Backend (445 tests)
cd backend && mvn test

# Frontend (54 tests)
cd frontend && npm test

# Android unit tests
cd android && ./gradlew testDebugUnitTest
```

## Project Structure

```
majetek/
├── .github/workflows/  # GitHub Actions CI/CD workflows
├── backend/            # Spring Boot REST API
├── frontend/           # React web application
├── android/            # Android mobile application
└── thesis/             # Bachelor's thesis (LaTeX) and defense slides
```

## Deployment

The system is deployed on a VPS with Nginx as a reverse proxy. See Chapter 7 of the thesis for details.
