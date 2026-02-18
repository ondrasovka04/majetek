# Majetek – Backend

REST API pro správu majetku (asset management) postavené na Spring Boot.

## Technologie

| Technologie | Verze | Účel |
|---|---|---|
| Java | 21 | Runtime |
| Spring Boot | 3.2.5 | Framework |
| PostgreSQL | 17 | Produkční databáze |
| H2 | — | Testovací in-memory databáze |
| JWT (jjwt) | 0.11.5 | Autentizace (access + refresh tokeny) |
| MapStruct | 1.5.5 | Mapování DTO ↔ Entity |
| Lombok | — | Boilerplate redukce |
| iText 7 | 8.0.0 | Generování PDF reportů |
| EasyExcel + POI | 4.0.3 / 5.4.0 | Excel export |
| SpringDoc OpenAPI | 2.4.0 | Swagger UI |
| JaCoCo | 0.8.11 | Code coverage (≥80 % line, ≥75 % branch) |

## Prerekvizity

- **Java 21+**
- **PostgreSQL** (produkce)
- Maven Wrapper je součástí repozitáře (`./mvnw`)

## Environmentální proměnné

| Proměnná | Popis |
|---|---|
| `DB_USERNAME` | Uživatel PostgreSQL |
| `DB_PASSWORD` | Heslo PostgreSQL |
| `JWT_SECRET` | Tajný klíč pro access tokeny (HS256) |
| `JWT_REFRESH_SECRET` | Tajný klíč pro refresh tokeny (HS256) |
| `ATTACHMENT_MAX_FILE_SIZE` | Max velikost přílohy (např. `10MB`) |

## Spuštění

```bash
# Kompilace + testy
./mvnw clean package

# Kompilace bez testů
./mvnw clean package -DskipTests

# Spuštění
java -jar target/majetek-0.0.1-SNAPSHOT.jar

# Pouze testy
./mvnw test
```

Aplikace běží na portu **8080**. Swagger UI je dostupný na `/swagger-ui.html`.

## Struktura projektu

```
src/main/java/cz/majetek/
├── auth/                  # Autentizace – login, registrace, JWT, refresh tokeny
├── card/                  # Karty majetku – CRUD, filtrování, exporty (PDF, Excel, ZPL)
├── attachment/            # Přílohy ke kartám – upload, download, smazání
├── dictionary/            # Číselníky (asset group, class, location, purchase/write-off method)
├── inventoryaudit/        # Inventarizace – hromadný import, rozdíly, projekce
├── entity/                # JPA entity sdílené across modulů
├── exception/             # Centralizované zpracování chyb (GlobalExceptionHandler)
├── config/                # Konfigurace (OpenAPI)
└── MajetekApplication.java
```

Každý modul sleduje pattern: **controller → service (interface + impl) → repository** s MapStruct mapperem a vlastními exception třídami.

## REST API

### Auth (`/api/auth`)
| Metoda | Endpoint | Popis |
|---|---|---|
| POST | `/login` | Přihlášení (Basic Auth → JWT + refresh cookie) |
| POST | `/refresh` | Rotace access + refresh tokenů |
| POST | `/register` | Registrace uživatele (pouze ADMIN) |
| PUT | `/change-password` | Změna hesla |
| GET | `/` | Seznam uživatelů |

### Karty majetku (`/api/card`)
| Metoda | Endpoint | Popis |
|---|---|---|
| GET | `/` | Všechny karty |
| GET | `/{id}` | Karta podle ID |
| GET | `/filter` | Filtrování karet |
| POST | `/` | Vytvoření karty |
| PATCH | `/{id}` | Aktualizace karty |
| DELETE | `/{id}` | Smazání karty |
| POST | `/print` | Tisk štítků (Zebra cloud) |
| POST | `/export-excel` | Excel export |
| POST | `/export-group-pdf` | PDF report skupiny |
| POST | `/export-location-pdf` | PDF report lokace |
| POST | `/export-card-pdf` | PDF report karty |

### Přílohy (`/api/card/{cardId}/attachments`)
| Metoda | Endpoint | Popis |
|---|---|---|
| POST | `/` | Upload přílohy |
| GET | `/` | Seznam příloh |
| GET | `/{id}` | Stažení přílohy |
| DELETE | `/{id}` | Smazání přílohy |

### Inventarizace (`/api/inventory-audit`)
| Metoda | Endpoint | Popis |
|---|---|---|
| GET | `/` | Všechny záznamy |
| GET | `/differences` | Rozdíly inventury |
| POST | `/insert-many` | Hromadný import |
| POST | `/project` | Projekce rozdílů |
| PATCH | `/{cardId}` | Aktualizace záznamu |

### Číselníky
Všechny číselníky mají CRUD endpointy (`GET /`, `GET /{id}`, `POST /`, `PATCH /{id}`, `DELETE /{id}`):

| Zdroj | Prefix |
|---|---|
| Skupiny majetku | `/api/asset-group` |
| Třídy | `/api/class` |
| Lokace | `/api/location` |
| Způsoby pořízení | `/api/purchase-method` |
| Způsoby odpisu | `/api/write-off-method` |

Lokace navíc podporuje `POST /print` pro tisk štítku.

## Autentizace

Aplikace používá dvě autentizační schémata:

1. **Basic Auth** – pouze pro `/api/auth/login`
2. **JWT Bearer** – pro všechny ostatní endpointy

Po přihlášení server vrátí access token (v response body, expirace 15 min) a refresh token (HttpOnly cookie, expirace 7 dní).

## Testování

```bash
# Všechny testy
./mvnw test

# JaCoCo report (po testech)
open target/site/jacoco/index.html
```

- **Produkční DB**: PostgreSQL
- **Testovací DB**: H2 in-memory (`application-test.properties`, profil `test`)
- **Unit testy**: controllery (MockMvc), services (Mockito), repository, security, exceptions
- **Integrační testy**: end-to-end přes celý Spring context s H2

JaCoCo vyžaduje ≥80 % line coverage a ≥75 % branch coverage. Z coverage jsou vyloučeny: DTO, entity, config, mapper, `MajetekApplication`.
