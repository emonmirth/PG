# Architecture: EonMirth Panel Guard

## Overview
EonMirth Panel Guard follows clean, single-module Android architecture with unidirectional data flow (Jetpack Compose + ViewModel/StateFlow).

## Module Layout
```
com.eonmirth.panelguard
├── MainActivity.kt
├── ui/
│   ├── AppRoot.kt
│   ├── HomeScreen.kt
│   ├── CompatibilityScreen.kt
│   ├── SetupScreen.kt
│   ├── VerificationScreen.kt
│   ├── DiagnosticsScreen.kt
│   └── components/
├── domain/
│   ├── ProtectionState.kt
│   ├── Capability.kt
│   └── OemFamily.kt
├── platform/
│   ├── DevicePolicyController.kt
│   ├── LockStateReader.kt
│   ├── OemCapabilityReader.kt
│   └── SettingsNavigator.kt
├── data/
│   ├── PreferencesRepository.kt
│   └── DiagnosticEvent.kt
└── util/
    └── Result.kt
```

## Core Components
- **OemCapabilityReader**: Detects device manufacturer, model, build release, and determines the highest legitimate protection strategy.
- **DevicePolicyController**: Detects whether the app is Device Owner or Profile Owner. Manages Lock Task mode with `LOCK_TASK_FEATURE_NONE` only in authorized managed deployments.
- **SettingsNavigator**: Dispatches verified public intents to relevant OEM settings screens with graceful manual fallback.
- **ProtectionStateEvaluator**: Assesses current protection state without making assumptions.
