# Asset Management – Android App for Zebra Scanners

Native Android application for asset management and inventory using barcode scanners. Primarily designed for **Zebra** handheld terminals running Android OS.

## Features

| Screen            | Description                                                                                                                                                                                                           |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Control Scan**  | Scan an asset barcode and instantly display card details (ID, name, price, quantity, location).                                                                                                                       |
| **Inventory**     | Two-step process: first scan a location (code starting with `L`), then scan asset cards. The app visually indicates whether the asset is in the correct location and displays inventory progress in a circular chart. |
| **Data Transfer** | Download cards and locations from the server to the local database, upload inventory results back to the server, clear inventory data, and configure server URL and credentials.                                      |

## Tech Stack

- **Language:** Java 25
- **Min SDK:** 33 (Android 13)
- **Target SDK:** 37
- **Build system:** Gradle Kotlin DSL, AGP 9.5.1, Gradle 9.9
- **Database:** Room 2.8.5 (SQLite) – tables `card`, `location`, `inventory_audit`
- **Networking:** OkHttp 5.5.1, Gson 2.15.0
- **Security:** EncryptedSharedPreferences (security-crypto 1.1.0) for JWT token and credentials, network security config (cleartext disabled), R8 obfuscation in release
- **UI:** Material Design 1.14, ConstraintLayout, BottomNavigationView, custom `CircularInventoryProgressView`

## Project Structure

```
app/src/main/java/cz/majetek/
├── MajetekApp.java                       # Application – Room database initialization
├── data/
│   ├── db/
│   │   ├── AppDatabase.java              # Room database (singleton, version 6)
│   │   └── dao/
│   │       ├── CardDao.java              # CRUD for asset cards
│   │       ├── LocationDao.java          # CRUD for locations
│   │       └── InventoryAuditDao.java    # CRUD for inventory audit records
│   ├── entity/
│   │   ├── CardEntity.java               # Asset card (id, name, price, quantity, location)
│   │   ├── LocationEntity.java           # Location (id, name)
│   │   └── InventoryAuditEntity.java     # Inventory record (card_id, real_location_id, timestamp)
│   ├── network/
│   │   ├── ApiClient.java                # HTTP client – login, download cards/locations, upload audit
│   │   └── LoginResult.java              # Record for login result (token, expiration)
│   └── repository/
│       └── DataSyncRepository.java       # Data synchronization (download/upload) with REST API
├── ui/
│   ├── activity/
│   │   └── MainActivity.java             # Main activity with BottomNavigationView
│   ├── fragment/
│   │   ├── ControlScanFragment.java      # Control scan – look up card by ID
│   │   ├── InventoryScanFragment.java    # Inventory scan – location → asset → save
│   │   └── DataTransferFragment.java     # Data transfer – download/upload/clear/configure
│   └── view/
│       └── CircularInventoryProgressView.java  # Custom circular inventory progress indicator
└── util/
    ├── BaseUrlStorage.java               # SharedPreferences for server URL
    ├── ScanInputHelper.java              # Utility for Zebra dual-listener scan pattern
    └── TokenStorage.java                 # EncryptedSharedPreferences for JWT token and credentials
```

## Prerequisites

- **Android Studio** Ladybug or newer
- **JDK 25+** (Gradle daemon toolchain is auto-provisioned)
- **Zebra scanner** with Android 13+ (or any Android 13+ device for testing)

## Configuration

### Server URL

Default API URL: `https://majetek.zs-ustavni.cz/api`

Can be changed in the app via **Data Transfer → Configure Connection**. The URL is stored in `SharedPreferences`.

### Credentials

Login credentials are entered in the connection configuration dialog and stored securely in `EncryptedSharedPreferences`.

## Build & Install

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

APK is generated in `app/build/outputs/apk/debug/`.

## Tests

```bash
# Unit tests (run locally on JVM)
./gradlew testDebugUnitTest

# Instrumented tests (require connected device/emulator)
./gradlew connectedDebugAndroidTest
```

| Type         | Directory              | Description                                                                                                                                                                                              |
| ------------ | ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Unit tests   | `app/src/test/`        | `DataSyncRepositoryTest` (4 tests – JSON serialization), `BaseUrlStorageTest` (9 tests – URL validation/sanitization), `ApiClientTest` (9 tests – MockWebServer: login, download, upload, error states)  |
| Instrumented | `app/src/androidTest/` | `DaoTest` (11 tests – Room DAO with in-memory DB), `TokenStorageTest` (7 tests – EncryptedSharedPreferences, expiry, credentials)                                                                        |

## REST API Endpoints

The app communicates with the backend via these endpoints:

| Method | Endpoint                             | Description                            |
| ------ | ------------------------------------ | -------------------------------------- |
| `POST` | `/auth/login`                        | Login (Basic Auth) → returns JWT token |
| `GET`  | `/card/filter?includeWriteOff=false` | Download asset card list               |
| `GET`  | `/location`                          | Download location list                 |
| `POST` | `/inventory-audit/insert-many`       | Upload inventory results (JSON array)  |

## Localization

The app is fully localized in Czech (`values/strings.xml`). Default language is Czech.

## Permissions

- `INTERNET` – communication with REST API
- `ACCESS_NETWORK_STATE` – Wi-Fi connectivity detection before data transfer
