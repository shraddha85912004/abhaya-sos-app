# 🛡️ Abhaya: Your Safety Companion

Abhaya is an advanced, fully-featured personal safety application designed natively for Android. It operates with a sleek, futuristic **"Space HUD"** interface powered by pure glassmorphism and integrates deep sensor capabilities to keep you safe in critical situations.

## ✨ Key Features

### 1. Hands-Free SOS Triggers 🚨
- **Voice Recognition:** Abhaya constantly runs a lightweight background service listening for your custom "Wakeword". Once triggered, it automatically fires off SOS alerts without you needing to touch the phone.
- **Shake Detection:** In situations where speaking isn't possible, vigorously shaking your device will instantly trigger the SOS protocol via the built-in accelerometer.
- **Customizable Wake-word:** The custom wakeword can be easily configured in the user profile settings and is saved locally on the device.

### 2. Native Radar & Nearby Services 🗺️
- **Completely Free API Integration:** The app integrates directly with the **OpenStreetMap Overpass API**.
- **Real-Time Data:** Instantly fetches the closest Hospitals, Police Stations, Fire Stations, and Pharmacies within a 5-kilometer radius.
- **Seamless Action:** Results are presented directly inside the app with a beautiful UI. Tapping an item with a phone number immediately dials it. Tapping a location without a number automatically opens Google Maps to provide instant navigation coordinates.

### 3. "Space HUD" Aesthetic & Glassmorphism 🌌
- The application completely breaks away from standard, boring app grids.
- **Deep Space Palette:** Features seamless midnight-indigo to pitch-black background gradients.
- **Pure Glassmorphism:** All UI elements, cards, and dashboards are styled as semi-transparent "frosted glass" panels with thin, glowing bright borders that catch the light.
- **HUD Layout:** The dashboard prominently features an asymmetrical "Heads Up Display" layout centering a pulsing, neon-magenta SOS core.

### 4. Comprehensive User Profile 👤
- **Emergency Settings:** Save crucial medical information (Blood group, allergies) and custom SOS message templates.
- **Form UI Polish:** All input forms are styled using high-end Material Design 3 `TextInputLayout` components, providing a professional floating-label interaction.

### 5. AI Safety Guide 🤖
- Features a built-in AI assistant to provide immediate guidance on emergency protocols, first aid, and safety tips.

## 🛠️ Technology Stack
- **Language:** Java
- **UI Framework:** Android XML, Material Design 3
- **Sensors used:** Microphone (`android.speech.SpeechRecognizer`), Accelerometer (`SensorManager`)
- **Location Services:** Google FusedLocationProvider
- **External APIs:**
  - OpenStreetMap Overpass API (For Maps/Nearby Places)
  - Gemini API (For AI Chat feature)

## 🚀 Installation
1. Clone or download the repository.
2. Open the project in Android Studio.
3. Build the project using `Gradle`.
4. Deploy the APK to any Android device running Android API 24 or higher. Note: Ensure you grant Location, Audio, and SMS permissions on the first launch for the background detection services to work optimally.
