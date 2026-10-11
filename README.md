# EonMirth Panel Guard (PG)

<p align="center">
  <img src="https://img.shields.io/badge/Language-Kotlin%202.1+-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Design-Material%203-757575.svg?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3" />
  <img src="https://img.shields.io/badge/Min%20SDK-29%20(Android%2010)-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white" alt="Android 10+" />
  <img src="https://img.shields.io/badge/Target%20SDK-36-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white" alt="Target SDK 36" />
  <img src="https://img.shields.io/badge/Ecosystem-EonMirth-0052CC.svg?style=for-the-badge&logo=googlechrome&logoColor=white" alt="EonMirth Ecosystem" />
</p>

> **Legitimate, evidence-based, capability-driven Android lock-screen notification shade & Quick Settings protection.**
> *An official security product in the [**EonMirth Ecosystem**](https://github.com/emonmirth/EonMirth-Web).*

---

## 🌐 EonMirth Ecosystem

**EonMirth Panel Guard (PG)** is developed as an official mobile security solution under the **EonMirth** brand.

* 🌐 **EonMirth Web Platform**: [github.com/emonmirth/EonMirth-Web](https://github.com/emonmirth/EonMirth-Web)
* 📱 **EonMirth Mobile Security**: [github.com/emonmirth/PG](https://github.com/emonmirth/PG)

---

## 🛡️ Core Purpose & Vision
**EonMirth Panel Guard** helps Android users protect their lock screen from unauthorized access to sensitive Quick Settings tiles (such as Wi-Fi, Mobile Data, Bluetooth, Location, and Airplane Mode) and the notification shade on supported devices.

Unlike legacy utilities that rely on dangerous hacks, EonMirth Panel Guard prioritizes **truthfulness, transparency, and 100% Google Play Policy compliance**.

---

## 🌟 Key Features

* **🔍 OEM Capability Detection Engine**: Automatically identifies device manufacturer and skin (Samsung One UI, Xiaomi HyperOS/MIUI, OPPO ColorOS, Google Pixel, Motorola) and presents native protection options.
* **📖 Step-by-Step Vendor Guidance**: Provides precise, official settings paths for major OEMs to turn off lock-screen shade access.
* **✅ Real-State Verification Evaluator**: Evaluates protection status based on concrete machine evidence rather than button clicks or false assumptions.
* **🔐 Enterprise Managed Mode (Device Owner)**: Offers strict `LOCK_TASK_FEATURE_NONE` Lock Task kiosk controls for organization-provisioned devices.
* **🎨 Material 3 Design & Dark Theme**: Modern, polished user interface with theme-adaptive status colors, icons, and smooth transitions.

---

## 🛡️ Strict Safety Boundaries & Guarantees

| Protection Method | Status | Reason |
|---|---|---|
| **OEM System Settings** | ✅ Supported | Uses official vendor toggles (Samsung, Xiaomi, OPPO, vivo). |
| **Android Enterprise Lock Task** | ✅ Supported | Uses public `DevicePolicyManager` APIs on Device Owner devices. |
| **Accessibility Service Interceptor Hacks** | ❌ Prohibited | Violates Google Play policies and creates security risks. |
| **Device Root / Hidden API Bypass** | ❌ Prohibited | Compromises system stability and security. |
| **Deceptive Overlays / Fake Locks** | ❌ Prohibited | Strictly prohibited by Google Play Store guidelines. |

---

## 📱 Supported Device Matrix

| Manufacturer | OS Skin | Native Shade Toggle | Strategy |
|---|---|---|---|
| **Samsung** | One UI | Yes ("Lock network and security") | OEM Guidance |
| **Xiaomi / POCO / Redmi** | HyperOS / MIUI | Yes ("Items shown on Lock screen") | OEM Guidance |
| **OPPO / Realme** | ColorOS / Realme UI | Yes ("Swipe down on Lock screen") | OEM Guidance |
| **OnePlus** | OxygenOS | Yes ("Lock screen drawer") | OEM Guidance |
| **vivo / iQOO** | Funtouch / OriginOS | Yes ("Lock screen settings") | OEM Guidance |
| **Google** | Stock Android | Enterprise / Lockdown | Lockdown / Device Owner Mode |
| **Motorola** | My UX / Hello UI | Enterprise / Lockdown | Lockdown / Device Owner Mode |

---

## 🏗️ Architecture & Tech Stack

* **Language**: Kotlin 2.1+
* **UI Framework**: Jetpack Compose + Material 3 (Dynamic Color & Dark Theme)
* **Architecture**: Single module (`:app`), Unidirectional Data Flow (UDF), Clean Architecture.
* **Build System**: Gradle 9.8+ with Kotlin DSL (`*.gradle.kts`) and Android Gradle Plugin 9.4+.

```
app/src/main/java/com/eonmirth/panelguard/
├── domain/                  # Pure Kotlin domain models & state definitions
│   ├── Capability.kt
│   ├── OemFamily.kt
│   ├── ProtectionState.kt
│   └── VerificationResult.kt
├── platform/                # Android System & DevicePolicyManager integration
│   ├── DevicePolicyController.kt
│   ├── LockStateReader.kt
│   ├── OemCapabilityReader.kt
│   └── VerificationEvaluator.kt
└── ui/                      # Jetpack Compose screens & Material 3 theme
    ├── theme/ (Color.kt, Theme.kt)
    ├── AppRoot.kt
    ├── HomeScreen.kt
    ├── CompatibilityScreen.kt
    ├── SetupScreen.kt
    └── VerificationScreen.kt
```

---

## 🛠️ Building & Testing

### Prerequisites
* Android Studio Ladybug (2024.2.1+) or newer
* JDK 17+ / JBR 21+
* Android SDK Platform 36

### Build Commands
```bash
# Clone the repository
git clone https://github.com/emonmirth/PG.git
cd PG

# Run all unit tests
./gradlew test

# Assemble Debug APK
./gradlew assembleDebug
```

---

## 📄 License & Copyright

Copyright © 2026 **[EonMirth](https://github.com/emonmirth/EonMirth-Web)**. All rights reserved.
Developed with high standards of security, privacy, and user experience.
