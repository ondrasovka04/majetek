# Asset Management – Backend

REST API for asset management built on Spring Boot.

## Technology

| Technology        | Version       | Purpose                                  |
|-------------------|---------------|------------------------------------------|
| Java              | 21            | Runtime                                  |
| Spring Boot       | 3.4.13        | Framework                                |
| PostgreSQL        | 17            | Production database                      |
| H2                | —             | In-memory test database                  |
| JWT (jjwt)        | 0.13.0        | Authentication (access + refresh tokens) |
| MapStruct         | 1.6.3         | DTO ↔ Entity mapping                     |
| Lombok            | —             | Boilerplate reduction                    |
| iText             | 9.5.0         | PDF report generation                    |
| EasyExcel + POI   | 4.0.3 / 5.5.1 | Excel export                             |
| SpringDoc OpenAPI | 2.8.17        | Swagger UI                               |
| Caffeine          | —             | In-memory cache (dictionaries)           |
| Actuator          | —             | Health check and monitoring              |
| JaCoCo            | 0.8.14        | Code coverage (≥80% line, ≥75% branch)   |

## Prerequisites

- **Java 25+**
- **PostgreSQL** (production)
- Maven Wrapper is included (`./mvnw`)

## Environment Variables

| Variable                   | Description                           |
|----------------------------|---------------------------------------|
| `DB_USERNAME`              | PostgreSQL user                       |
| `DB_PASSWORD`              | PostgreSQL password                   |
| `JWT_SECRET`               | Secret key for access tokens (HS256)  |
| `JWT_REFRESH_SECRET`       | Secret key for refresh tokens (HS256) |
| `ATTACHMENT_MAX_FILE_SIZE` | Max attachment size (e.g. `10MB`)     |

## Running

```bash
# Compile + tests
./mvnw clean package

# Compile without tests
./mvnw clean package -DskipTests

# Run
java -jar target/majetek-0.0.1-SNAPSHOT.jar

# Tests only
./mvnw test
```

The application runs on port **8080**. Swagger UI is available at `/swagger-ui.html`.

## Project Structure

```
src/main/java/cz/majetek/
├── auth/                  # Authentication – login, registration, JWT, refresh tokens
├── card/                  # Asset cards – CRUD, filtering, exports (PDF, Excel, ZPL)
├── attachment/            # Card attachments – upload, download, deletion
├── dictionary/            # Dictionaries (asset group, class, location, purchase/write-off method)
├── inventoryaudit/        # Inventory audit – bulk import, differences, projection
├── entity/                # JPA entities shared across modules
├── exception/             # Centralized error handling (GlobalExceptionHandler)
├── config/                # Configuration (OpenAPI)
└── MajetekApplication.java
```

Each module follows the pattern: **controller → service (interface + impl) → repository** with a MapStruct mapper and
custom exception classes.

## REST API

### Auth (`/api/auth`)

| Method | Endpoint           | Description                               |
|--------|--------------------|-------------------------------------------|
| POST   | `/login`           | Login (Basic Auth → JWT + refresh cookie) |
| POST   | `/refresh`         | Access + refresh token rotation           |
| POST   | `/register`        | User registration (ADMIN only)            |
| PUT    | `/change-password` | Change password                           |
| GET    | `/`                | List users                                |

### Asset Cards (`/api/card`)

| Method | Endpoint               | Description                |
|--------|------------------------|----------------------------|
| GET    | `/`                    | All cards                  |
| GET    | `/{id}`                | Card by ID                 |
| GET    | `/filter`              | Filter cards               |
| POST   | `/`                    | Create card                |
| PATCH  | `/{id}`                | Update card                |
| DELETE | `/{id}`                | Delete card                |
| POST   | `/print`               | Print labels (Zebra cloud) |
| POST   | `/export-excel`        | Excel export               |
| POST   | `/export-group-pdf`    | Group PDF report           |
| POST   | `/export-location-pdf` | Location PDF report        |
| POST   | `/export-card-pdf`     | Card PDF report            |

### Attachments (`/api/card/{cardId}/attachments`)

| Method | Endpoint | Description         |
|--------|----------|---------------------|
| POST   | `/`      | Upload attachment   |
| GET    | `/`      | List attachments    |
| GET    | `/{id}`  | Download attachment |
| DELETE | `/{id}`  | Delete attachment   |

### Inventory Audit (`/api/inventory-audit`)

| Method | Endpoint       | Description           |
|--------|----------------|-----------------------|
| GET    | `/`            | All records           |
| GET    | `/differences` | Inventory differences |
| POST   | `/insert-many` | Bulk import           |
| POST   | `/project`     | Project differences   |
| PATCH  | `/{cardId}`    | Update record         |

### Dictionaries

All dictionaries have CRUD endpoints (`GET /`, `GET /{id}`, `POST /`, `PATCH /{id}`, `DELETE /{id}`):

| Resource          | Prefix                  |
|-------------------|-------------------------|
| Asset groups      | `/api/asset-group`      |
| Classes           | `/api/class`            |
| Locations         | `/api/location`         |
| Purchase methods  | `/api/purchase-method`  |
| Write-off methods | `/api/write-off-method` |

Locations also support `POST /print` for label printing.

## Authentication

The application uses two authentication schemes:

1. **Basic Auth** – only for `/api/auth/login`
2. **JWT Bearer** – for all other endpoints

After login, the server returns an access token (in response body, expires in 15 min) and a refresh token (HttpOnly
cookie, expires in 7 days).

## Testing

```bash
# All tests
./mvnw test

# JaCoCo report (after tests)
open target/site/jacoco/index.html
```

- **Production DB**: PostgreSQL
- **Test DB**: H2 in-memory (`application-test.properties`, profile `test`)
- **Unit tests**: controllers (MockMvc), services (Mockito), repository, security, exceptions
- **Integration tests**: end-to-end through the full Spring context with H2

JaCoCo requires ≥80% line coverage and ≥75% branch coverage. Excluded from coverage: DTOs, entities, config, mappers,
`MajetekApplication`.
