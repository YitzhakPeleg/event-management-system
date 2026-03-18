# Event Management System

## Prerequisites

| Tool | Version | Install |
|---|---|---|
| Java JDK | 11+ | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| Android SDK | API 34 | Via Android Studio or `sdkmanager` |

---

## 1. Database Setup

```bash
# Create the database, all tables, and seed data
mysql -u root -p < db/schema.sql
```

---

## 2. Backend (Java + Tomcat)

```bash
cd backend

# First time only — download dependencies
mvn dependency:resolve

# Build and run (starts Tomcat on http://localhost:8888/EventManagement)
mvn tomcat7:run
```

The web app is now available at:
- **Login page:** http://localhost:8888/EventManagement/login
- **API base:** http://localhost:8888/EventManagement/api/

To stop: press `Ctrl+C`

> **Before running:** open `src/util/DBConnection.java` and update `DB_PASSWORD` to your MySQL password.

---

## 3. Android App

### Option A — Android Studio (recommended for first time)
Open the `android/` folder in Android Studio and click Run.

### Option B — CLI

```bash
cd android

# List available emulators
emulator -list-avds

# Start an emulator (replace Pixel_6 with your AVD name)
emulator -avd Pixel_6 &

# Wait for emulator to boot, then build and install
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.eventmanagement/.ui.LoginActivity
```

> **Note:** The Android app connects to `http://10.0.2.2:8888/EventManagement`
> (`10.0.2.2` is the emulator's address for the host machine's `localhost`).
> If using a **physical device** on the same Wi-Fi, change this in `ApiClient.java`
> to your computer's local IP (e.g. `http://192.168.1.100:8888/EventManagement`).

---

## 4. Presentation

Open `presentation.html` in any browser.
Navigate with ← → arrow keys or the on-screen buttons.

---

## Default Login Credentials

| Username | Password | Role |
|---|---|---|
| admin | Admin1234 | ADMIN |
| yossi | Yossi1234 | USER |
| dana | Dana1234 | USER |
| moshe | Moshe1234 | USER |
| sarah | Sarah1234 | USER |
| david | David1234 | USER |
| rachel | Rachel1234 | USER |
| avi | Avi1234 | USER |
| tamar | Tamar1234 | USER |
| noam | Noam1234 | USER |
| maya | Maya1234 | USER |
| itay | Itay1234 | USER |
| noa | Noa1234 | USER |

---

## Project Structure

```
event-management-system/
├── db/schema.sql               ← Run once to set up MySQL
├── backend/                    ← Java Web App (Maven)
│   ├── pom.xml                 ← Dependencies + Tomcat plugin
│   └── src/ ...
├── android/                    ← Android App (Gradle)
│   ├── build.gradle
│   ├── app/build.gradle        ← Dependencies (ZXing, RecyclerView...)
│   └── app/src/ ...
├── presentation.html           ← Exam presentation (open in browser)
└── README.md                   ← This file
```
