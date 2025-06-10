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

Orphans2/ ├── app/ 
# Application source code ├── build.gradle.kts 
# Project-level Gradle settings ├── settings.gradle.kts 
# Module declaration ├── gradle/
# Gradle wrapper └── local.properties
# Local settings (e.g., SDK path)
