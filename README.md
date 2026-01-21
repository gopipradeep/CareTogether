# 🤝 CareTogether - Android Application

**CareTogether** is a modern, purpose-driven Android application designed to bridge the gap between donors and orphanages. The app facilitates real-time support for children in need and slum areas by allowing users to report orphans, organizations to post their needs, and donors to find exactly where their help is required.

---

## 🚀 Key User Roles

### 👑 Admin Dashboard
*   **Live Statistics:** View the total number of registered Donors and Organizations in real-time.
*   **Feedback Management:** Read and moderate feedback submitted by users, including their Gmail addresses and star ratings.

### 👤 Donor Interface
*   **Orphanage Search:** Find organizations filtered by State and District.
*   **Real-time Needs:** View the specific needs (food, clothes, books, etc.) posted by organizations.
*   **Slum Area Reporting:** Report orphan children in slum areas by providing location details and uploading photos.
*   **Profile Management:** Maintain personal details for a personalized experience.

### 🏢 Organization Interface
*   **Instant Profile:** High-speed access to organization details using local caching.
*   **Post Needs:** Create and publish real-time requests for items or support.
*   **Community Reports:** View reports of orphans found in their vicinity to take immediate action.

---

## 🛠 Tech Stack

- **Language:** Kotlin 
- **Database & Auth:** Firebase Firestore, Authentication, and Storage.
- **UI Components:** Material Design 3, CoordinatorLayout, ConstraintLayout.
- **Image Handling:** Glide (Supports both URL and Base64 encoded images).
- **Architecture:** MVVM patterns with DataBinding & ViewBinding.
- **Caching:** SharedPreferences with Gson for instant profile loading.

---

## 🏁 How to Run the Application

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/your-username/CareTogether.git
```

### 2️⃣ Firebase Configuration (Required)
The app requires Firebase to function. Follow these steps:
1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Create a project named **CareTogether**.
3.  Add an Android App with the package name `com.example.orphans`.
4.  **Download `google-services.json`** and place it in the `app/` directory.
5.  **Enable Auth & Firestore:**
    *   Enable **Email/Password** and **Google** sign-in providers in Firebase Authentication.
    *   Enable **Cloud Firestore** in test mode.

### 3️⃣ Add SHA-1 Fingerprint
To enable Google Sign-In:
1.  Open Terminal in Android Studio and run `./gradlew signingReport`.
2.  Copy the **SHA-1** key from the `debug` variant.
3.  Add this fingerprint to your App settings in the Firebase Console.

### 4️⃣ Build and Install
1.  Sync the project with Gradle files.
2.  Connect your Android device via USB (with USB Debugging enabled).
3.  Click **Run** in Android Studio.

---

## 📖 How to Use

### User Registration
1.  Select **Sign Up** on the login screen.
2.  After entering your email and password, you **must** complete the "Details Form".
3.  **Note:** If you press the back button before finishing the details form, the account creation will be cancelled, and you will need to start again. This ensures all users in the system have a complete profile.

---

## 📁 Project Structure
```
CareTogether/
├── app/
│   ├── src/main/java/com/example/orphans/  # Kotlin Source Code
│   ├── src/main/res/layout/                # UI XML Files
│   ├── src/main/res/drawable/              # App Icons & Gradients
│   └── google-services.json                # Firebase Config
├── build.gradle.kts                        # Build configuration
└── gradlew.bat                             # Gradle wrapper
```
---
*Developed with ❤️ to support children in need.*
