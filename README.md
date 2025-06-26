# Book Poll App

A modern Android application for managing and sharing book collections among students. Built with Kotlin and Firebase, this app allows users to create, manage, and share their book reading lists with status tracking and personal thoughts.

## Features

- **User Authentication**: Secure login and registration using Firebase Authentication
- **Book Management**: Add, edit, and delete books from your personal collection
- **Status Tracking**: Mark books as READ or UNREAD with easy toggle functionality
- **Personal Thoughts**: Add personal notes and thoughts about each book
- **Book Cover Photos**: Take a photo of a book cover using your device's camera and attach it to a book entry
- **Book Details**: Comprehensive view of book information including creation and update timestamps
- **Modern UI**: A clean, intuitive interface
- **Responsive Design**: Optimized for various screen sizes and orientations

## Book Cover Photo Feature

You can now add a photo of your book's cover when creating or editing a book entry:

- Tap the **"Take Photo"** button in the "Add Book" or "Edit Book" screen.
- The app will request camera permission if not already granted.
- Take a photo of the book cover. The photo will be shown as a preview.
- You can remove the photo before saving if you wish.
- Once saved, the book cover will be displayed in the book details view.

**Where is the photo saved?**
- The photo is saved **locally** in the app's private storage on your device (not in the cloud or shared with other apps).
- The file is not uploaded to Firebase or any external server.

**Privacy Note:**
- Book cover photos are stored securely and are not accessible by other apps or users.
- If you uninstall the app, the photos will be deleted from your device.

## Screenshots

The app includes the following main screens:
- Login/Registration screens
- Main book list with add functionality
- Book details view
- Create/Edit book form
- Password reset functionality

## Requirements

### Development Environment
- **Android Studio**: Latest version (recommended: Android Studio Hedgehog or newer)
- **JDK**: Version 11 or higher
- **Kotlin**: Version 1.9.24
- **Android SDK**: API level 35 (Android 15)
- **Minimum SDK**: API level 24 (Android 7.0)

### Runtime Requirements
- **Android Device/Emulator**: Android 7.0 (API 24) or higher
- **Internet Connection**: Required for Firebase services
- **Google Services**: Firebase project setup required

## Installation

### Prerequisites
1. Install [Android Studio](https://developer.android.com/studio)
2. Install JDK 11 or higher
3. Set up Android SDK with API level 35

### Setup Steps

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd mobile_app
   ```

2. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication with Email/Password sign-in method
   - Download the `google-services.json` file
   - Place the `google-services.json` file in the `app/` directory

3. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the project directory and select it

4. **Sync Project**
   - Wait for Gradle sync to complete
   - If prompted, update any dependencies

5. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon) in Android Studio
   - Select your target device and click "OK"

### Alternative: Command Line Build

```bash
# Navigate to project directory
cd mobile_app

# Build the project
./gradlew build

# Install on connected device
./gradlew installDebug

# Run the app
./gradlew runDebug
```

## Project Structure

```
mobile_app/
├── app/
│   ├── build.gradle.kts              # App-level build configuration
│   ├── google-services.json          # Firebase configuration (add manually)
│   ├── proguard-rules.pro           # ProGuard rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml   # App manifest
│           ├── java/
│           │   └── com/example/fhj_student_app_part1/
│           │       ├── models/
│           │       │   └── Book.kt   # Book data model
│           │       ├── repository/
│           │       │   └── BookRepository.kt  # Data access layer
│           │       ├── BookDetailsActivity.kt # Book details screen
│           │       ├── CreateBookActivity.kt  # Add/Edit book screen
│           │       ├── ForgotPasswordActivity.kt # Password reset
│           │       ├── LoginActivity.kt       # Login screen
│           │       ├── MainActivity.kt        # Main book list
│           │       └── RegisterActivity.kt    # Registration screen
│           └── res/
│               ├── drawable/         # App icons and drawables
│               ├── layout/           # UI layout files
│               ├── values/           # Strings, colors, themes
│               └── mipmap/          # App launcher icons
├── build.gradle.kts                  # Project-level build configuration
├── gradle/
│   └── libs.versions.toml           # Dependency version management
├── gradle.properties                 # Gradle properties
└── settings.gradle.kts              # Project settings
```

## Architecture

The app follows a simple but effective architecture:

### Data Layer
- **Book Model**: Data class representing a book with all necessary properties
- **BookRepository**: Handles all data operations with Firebase
- **BookStatus**: Enum for tracking read/unread status

### Presentation Layer
- **Activities**: Each screen is implemented as an Android Activity
- **Material Design**: Modern UI components from Material Design 3
- **ConstraintLayout**: Flexible and responsive layouts

### Key Components

#### Activities
- `LoginActivity`: Entry point with authentication
- `RegisterActivity`: User registration
- `MainActivity`: Book list and main navigation
- `CreateBookActivity`: Add/edit book functionality
- `BookDetailsActivity`: Detailed book view
- `ForgotPasswordActivity`: Password reset

#### Models
- `Book`: Data class with properties for id, title, author, status, owner, thoughts, and timestamps
- `BookStatus`: Enum with READ and UNREAD states

## Configuration

### Firebase Configuration
1. Create a Firebase project
2. Enable Authentication with Email/Password
3. Download `google-services.json`
4. Place it in the `app/` directory

### Build Configuration
- **Compile SDK**: 35 (Android 15)
- **Target SDK**: 35
- **Minimum SDK**: 24 (Android 7.0)
- **Java Version**: 11

## Usage

1. **First Launch**: Register a new account or login with existing credentials
2. **Add Books**: Tap the + button to add your first book
3. **Manage Books**: 
   - Tap on a book to view details
   - Use the toggle button to change read status
   - Edit or delete books you own
4. **Share**: Books are visible to all users, but only owners can modify them

## Authors
Mihajlo Ilijic
Oleksandra Annawitt
Paria Nikparsa

**Note**: This app requires a Firebase project to be set up before it can be used. Follow the Firebase setup instructions in the installation section. 