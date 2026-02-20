# Majetek – Android aplikace pro Zebra skenery

Mobilní Android aplikace pro evidenci a inventarizaci majetku pomocí čárových kódů. Určena primárně pro ruční skenery **Zebra** s Android OS.

## Funkce

| Obrazovka | Popis |
|---|---|
| **Kontrolní snímání** | Naskenování čárového kódu majetku a okamžité zobrazení detailu karty (ID, název, cena, množství, umístění). |
| **Inventura** | Dvou-krokový proces: nejprve se naskenuje lokace (kód začínající `L`), pak se postupně skenují karty majetku. Aplikace vizuálně signalizuje, zda se majetek nachází ve správném umístění, a zobrazuje průběh inventury v kruhovém grafu. |
| **Přenos dat** | Stažení karet a lokací ze serveru do lokální databáze, odeslání výsledků inventury zpět na server, vymazání inventurních dat a nastavení URL serveru. |

## Technický stack

- **Jazyk:** Java 21
- **Min SDK:** 33 (Android 13)
- **Target SDK:** 36
- **Build systém:** Gradle Kotlin DSL, AGP 8.13.0
- **Databáze:** Room 2.8.2 (SQLite) – tabulky `card`, `location`, `inventory_audit`
- **Síťová komunikace:** OkHttp 5.2.1, Gson 2.13.2
- **Bezpečnost:** EncryptedSharedPreferences (security-crypto) pro JWT token, network security config (cleartext zakázán), R8 obfuskace v release
- **UI:** Material Design 1.13, ConstraintLayout, BottomNavigationView, vlastní `CircularInventoryProgressView`

## Struktura projektu

```
app/src/main/java/cz/majetek/
├── MajetekApp.java                       # Application – inicializace Room databáze
├── data/
│   ├── db/
│   │   ├── AppDatabase.java              # Room databáze (singleton, verze 6)
│   │   └── dao/
│   │       ├── CardDao.java              # CRUD pro karty majetku
│   │       ├── LocationDao.java          # CRUD pro lokace / umístění
│   │       └── InventoryAuditDao.java    # CRUD pro záznamy inventury
│   ├── entity/
│   │   ├── CardEntity.java               # Karta majetku (id, název, cena, množství, lokace)
│   │   ├── LocationEntity.java           # Umístění (id, název)
│   │   └── InventoryAuditEntity.java     # Záznam inventury (card_id, real_location_id, timestamp)
│   ├── network/
│   │   ├── ApiClient.java                # HTTP klient – login, stažení karet/lokací, upload auditu
│   │   └── LoginResult.java              # Record pro výsledek loginu (token, expirace)
│   └── repository/
│       └── DataSyncRepository.java       # Synchronizace dat (download/upload) s REST API
├── ui/
│   ├── activity/
│   │   └── MainActivity.java             # Hlavní aktivita s BottomNavigationView
│   ├── fragment/
│   │   ├── ControlScanFragment.java      # Kontrolní snímání – vyhledání karty podle ID
│   │   ├── InventoryScanFragment.java    # Inventurní snímání – lokace → majetek → uložení
│   │   └── DataTransferFragment.java     # Přenos dat – download/upload/vymazání/konfigurace URL
│   └── view/
│       └── CircularInventoryProgressView.java  # Vlastní kruhový ukazatel průběhu inventury
└── util/
    ├── BaseUrlStorage.java               # SharedPreferences pro URL serveru
    └── TokenStorage.java                 # EncryptedSharedPreferences pro JWT token s expirací
```

## Požadavky

- **Android Studio** Ladybug nebo novější
- **JDK 21+**
- **Zebra skener** s Android 13+ (nebo jakýkoli Android 13+ zařízení pro testování)

## Konfigurace

### local.properties

Do souboru `local.properties` v kořeni projektu přidejte přihlašovací údaje k API:

```properties
ADMIN_USER=vase_uzivatelske_jmeno
ADMIN_PASS=vase_heslo
```

Tyto hodnoty se injektují jako `BuildConfig.ADMIN_USER` a `BuildConfig.ADMIN_PASS` a používají se pro Basic Auth přihlášení k REST API.

### URL serveru

Výchozí URL API: `https://majetek.zs-ustavni.cz/api`

Lze změnit přímo v aplikaci přes obrazovku **Přenos dat → Nastavit server**. URL se ukládá do `SharedPreferences` a musí začínat `https://`.

## Build & instalace

```bash
# Debug build
./gradlew assembleDebug

# Instalace na připojené zařízení
./gradlew installDebug
```

APK se vygeneruje v `app/build/outputs/apk/debug/`.

## Testy

```bash
# Unit testy (běží lokálně na JVM)
./gradlew testDebugUnitTest

# Instrumentované testy (vyžadují připojené zařízení/emulátor)
./gradlew connectedDebugAndroidTest
```

| Typ | Složka | Popis |
|---|---|---|
| Unit testy | `app/src/test/` | `DataSyncRepositoryTest` (4 testy – JSON serializace), `BaseUrlStorageTest` (9 testů – validace/sanitizace URL), `ApiClientTest` (9 testů – MockWebServer: login, download, upload, chybové stavy) |
| Instrumentované | `app/src/androidTest/` | `DaoTest` (11 testů – Room DAO s in-memory DB), `TokenStorageTest` (6 testů – EncryptedSharedPreferences, expiry, legacy cleanup) |

## REST API endpointy

Aplikace komunikuje s backendem přes tyto endpointy:

| Metoda | Endpoint | Popis |
|---|---|---|
| `POST` | `/auth/login` | Přihlášení (Basic Auth) → vrací JWT token |
| `GET` | `/card/filter?includeWriteOff=false` | Stažení seznamu karet majetku |
| `GET` | `/location` | Stažení seznamu lokací/umístění |
| `POST` | `/inventory-audit/insert-many` | Odeslání výsledků inventury (JSON pole) |

## Lokalizace

Aplikace má plnou českou lokalizaci (`values/strings.xml`). Výchozí jazyk je čeština.

## Oprávnění

- `INTERNET` – komunikace s REST API
- `ACCESS_NETWORK_STATE` – detekce Wi-Fi připojení před přenosem dat
