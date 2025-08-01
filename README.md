# 🤝 CareTogether - Android App

CareTogether is a purpose-driven Android application that bridges the gap between donors and orphanages, enabling support for children in need and slum areas. It offers streamlined features for reporting, connecting, and assisting with transparency and accessibility.

---

## 📲 Features

- 🏠 **Home Dashboard** with quick access to:
  - Orphanages
  - Slum Areas
  - Needs
  - Report Orphans
  - Help/Feedback
- 📍 **Location-based search** by city and district
- 🔐 **User authentication** (Login & Signup) with role selection (Donor or Organization)
- 🧾 **Profile management** for both donors and organizations
- 💬 **Feedback and Reporting System**
- 🌐 Future support for APIs (Yelp, Mapple, etc.)
- 🎨 Clean, user-friendly UI with Material Design

---

## 🛠 Tech Stack

- **Language:** Kotlin  
- **Backend:** Firebase Authentication & Firestore  
- **Libraries/Tools:**
  - Android Jetpack (ViewBinding, Navigation)
  - Material Design Components
  - Firebase SDKs
  - Gradle Kotlin DSL

---

## 📁 Project Structure
```
Orphans2/
├── app/ # Main source code
├── build.gradle.kts # Project-level Gradle config
├── settings.gradle.kts # Module declarations
├── gradle/ # Gradle wrapper files
└── local.properties # Local SDK path
```

---

## 🔧 Firebase Setup (Required for Authentication & Google Sign-In)

### 📥 Step 1: Add `google-services.json`

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Create or select your project.
3. Click ⚙️ **Project Settings** → **General**.
4. Under **Your Apps**, click **Add App** → Select **Android**.
5. Enter package name (e.g., `com.example.orphans`) and register.
6. Download the `google-services.json` file.
7. Place it inside:
app/google-services.json

---

### 🔐 Step 2: Add SHA-1 and SHA-256 Keys

1. In terminal, run:
```bash
./gradlew signingReport
```

1.Copy SHA-1 and SHA-256 from debug variant.

2.Go back to Firebase Console → Project Settings → General.

3.Under your Android app, click ✎ and paste the SHA keys.

4.Click Save.

🔄 Re-download google-services.json after this step and replace the old file.

---

### 🌐 Step 3: Get Web Client ID for Google Sign-In
1.In Firebase Console → Project Settings → General.

2.Scroll to Your apps → Web App (add one if needed).

3.Copy the Web Client ID under OAuth 2.0 client IDs.

---

### 🛠 Step 4: Add Web Client ID to strings.xml
```
<!-- app/src/main/res/values/strings.xml -->
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>
```
---

### ⚙️ Step 5: Add Google Services Plugin

In build.gradle.kts (Project-level):
```
classpath("com.google.gms:google-services:4.3.15") // or latest
```
In build.gradle.kts (App-level):
```
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}
```
Sync the project after adding.

