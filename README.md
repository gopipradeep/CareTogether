# CareTogether - Android App

CareTogether is an Android application built to connect donors and organizations to help orphanages and children in need. The app provides an easy way to explore orphanages, raise support for slum areas, report needs,report orphans and offer feedback.

## Features

- 🏠 Home Dashboard with four main sections:
  - Orphanages
  - Slum Areas
  - Needs
  - Report Orphans
  - Help/Feedback
- 📍 Location search based on district and city .
- 🔒 User authentication (Login, Signup) with role selection.
- 🎨 Clean and user-friendly UI.
- 📱 Firebase backend integration for user management.
- 📋 Profile management for donors and organizations.
- 📈 Future extension for API integrations (Yelp, Mapple APIs).

## Tech Stack

- **Language:** Kotlin
- **Backend:** Firebase Authentication & Firestore
- **Libraries/Tools:**
  - Android Jetpack (ViewBinding, Navigation)
  - Material Design Components
  - Gradle Kotlin DSL (`build.gradle.kts`)

## Project Structure
```
Orphans2/
├── app/ # Application source code 
├── build.gradle.kts # Project-level Gradle settings 
├── settings.gradle.kts# Module declaration 
├── gradle/# Gradle wrapper 
└── local.properties # Local settings (e.g., SDK path)
```
-----------------------------------------------------------------------------------------------------------------------------------------------
**FOLLOW THESE STEPS TO SET UP AND ENABLE FULL FUNCTIONALITY OF FIREBASE AND GOOGLE SIGN-IN:**

🔧** How to Add google-services.json & Web Client ID**

📥** Step 1: Get google-services.json**
Go to Firebase Console.

1.Select your project or create a new one.

2.Click on Project Settings (gear icon).

3.Under the General tab, scroll to Your apps section.

4.Click Add App and select Android.

5.Enter your package name (e.g., com.example.orphans) and register the app.

6.Download the google-services.json file.

7.Place it in your project at:
app/google-services.json


🌐 **Step 2: Get Web Client ID**
1.From the Firebase Console, go to Project Settings > General.

2.Scroll down to Your apps > Web App (or add a web app if none exists).

3.Copy the Web Client ID listed under OAuth 2.0 client IDs.


🛠** Step 3: Add to strings.xml**
1.Open:
app/src/main/res/values/strings.xml

2.Add this line:
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>

✅** Step 4: Add Plugin & Sync**

1.In your build.gradle files:
Project-level build.gradle:
classpath 'com.google.gms:google-services:4.3.15' // or latest

2.App-level build.gradle:
apply plugin: 'com.google.gms.google-services'

Then click Sync Now in Android Studio.

