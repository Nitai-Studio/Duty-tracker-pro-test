# Duty Tracker Pro - Ultimate Setup and Building Guide

Welcome to **Duty Tracker Pro**, a 100% native Android application designed elegantly using **Kotlin**, **Jetpack Compose**, **Room Database**, and **Firebase Realtime Database**. This document outlines the step-by-step setup, configuration, and production build instructions to successfully publish the application to the Google Play Store.

---

## 🚀 Key Architectural Architecture
1. **Model-View-ViewModel (MVVM)**: Separates the UI layer (Compose) from the state & business logic layer (`DutyViewModel`).
2. **Local & Cloud Syncing**: Data saved locally via **Room DB** automatically syncs to **Firebase Realtime Database** when online.
3. **Google Play Billing**: Bulletproof automated subscriptions (`premium_monthly` and `premium_yearly`) with verification pipelines.
4. **Offline Resilience**: Users can register, marked roster, log advances completely offline and sync once connection resumes.

---

## 📂 Configuration Checklists

### 1. Firebase Configuration Setup
This application utilizes several Firebase services: Authentication, Realtime Database, and Cloud Storage.

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add Project** and register a project named `duty-tracker-pro` (or any custom identifier).
3. Under the Project overview page, click the **Android** logo to register your Android app:
   - **Android package name (applicationId)**: `io.github.nitaistudio.twa`
   - **App nickname**: `Duty Tracker Pro`
   - **SHA-1 fingerprint**: Required if Google Login or PIN resets are performed (obtain yours via `./gradlew signingReport` in terminal).
4. Download the `google-services.json` config file and place it under `/app/` directory of this codebase.
5. In firebase console, navigate to:
   - **Authentication**: Enable **Email/Password** sign-in providers.
   - **Realtime Database**: Initialize a database instance. Ensure database rules are set to:
     ```json
     {
       "rules": {
         "config": {
           ".read": true,
           ".write": "auth != null"
         },
         "u": {
           "$uid": {
             ".read": "auth != null && auth.uid == $uid",
             ".write": "auth != null && auth.uid == $uid"
           }
         },
         "refList": {
           ".read": "auth != null",
           ".write": "auth != null"
         },
         "binance_payments": {
           "$uid": {
             ".read": "auth != null && auth.uid == $uid",
             ".write": "auth != null && auth.uid == $uid"
           }
         }
       }
     }
     ```
   - **Cloud Storage**: Initialize storage with default rules (for avatar profile upload handling).

### 2. Google Play Console Setup (Subscriptions)
To leverage the implemented `PlayBillingManager`:

1. Open your [Google Play Console Account](https://play.google.com/console).
2. Create/Select your application and navigate to **Monetize** -> **Products** -> **Subscriptions**.
3. Create two main Subscription Products matching the exact IDs declared in our codebase:
   - **ID**: `premium_monthly` (Configured Base Plan Price: ₹29 / month)
   - **ID**: `premium_yearly` (Configured Base Plan Price: ₹299 / year)
4. Activate the products and base plans.
5. In Play Console, navigate to **API Access** and link a Google Cloud Service Account to automate status validations if desired.

### 3. Start.io Ads Setup (Interstitial, Banner & Rewards)
This app includes high-yield ad blocks for free tier users, which automatically disappear for Premium subscribed accounts.

1. Go to the [Start.io Portal](https://portal.start.io/).
2. Create an App Entry, obtain the `App ID` for Android.
3. In this repository, update the App ID in `/app/src/main/java/com/example/viewmodels/DutyViewModel.kt` or keep the default validated testing id:
   - App ID: `205257935`

---

## 🛠️ Compilation and Native Release Build Guide

### Prerequisites
- Install **Android Studio Ladybug** or higher.
- JDK 17+ installed and path configured as default Gradle JVM.

### Build Steps inside Android Studio
1. Open Android Studio, select **File -> Open** and choose this project root folder.
2. Let Android Studio perform initial project sync and dependency checks.
3. To generate the **Production APK / Android App Bundle (AAB)**:
   - Navigate to top menu bar and select **Build > Generate Signed Bundle / APK...**
   - Choose **Android App Bundle (AAB)** for Play Store upload or **APK** for direct device installation.
   - Select or configure your secure keystore signature files.
   - Compile under `release` variant.

Your signed output will be saved inside `/app/release/` ready for Play Store launch!

---

## 🛡️ Support, Maintenance and Safe Modes
This app contains advanced cloud configuration systems allowing remote administration from Firebase without recompiling:
- **Maintenance Mode**: Toggle `config/maintenance/active` to `true` inside Realtime Database to safely lock the app during server updates.
- **Forced Update Mode**: Update `config/version/minVersion` to enforce version security gates on outdated user clients.
