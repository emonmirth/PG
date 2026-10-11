# EonMirth Panel Guard

> Legitimate, capability-driven Android lock-screen notification shade & Quick Settings protection.

## Core Purpose
EonMirth Panel Guard is designed to help users protect their lock screen from unauthorized access to the notification shade and Quick Settings tiles (such as Wi-Fi, mobile data, and airplane mode) on devices that support it.

## Philosophy & Guarantees
- **Truthful Status**: Never claims 100% theft protection. Reports actual capabilities honestly.
- **No Hacks**: Zero root, zero hidden APIs, zero accessibility race-condition shade blockers, zero deceptive overlays.
- **Two Distinct Paths**:
  1. **Consumer OEM Guidance**: Leverages native OEM settings where manufacturers (Samsung, Xiaomi, OPPO, etc.) provide official lock-screen shade toggles.
  2. **Managed Device (Device Owner)**: Utilizes official Android Enterprise Lock Task APIs with `LOCK_TASK_FEATURE_NONE` on properly provisioned enterprise/kiosk devices.

Managed controls appear only when Android reports the app as Device Owner; the app cannot grant itself that role. Entering Lock Task requires an unlocked device and explicit confirmation. `LOCK_TASK_FEATURE_NONE` also disables the system power-actions dialog while active. The managed screen includes an exit action that removes this app from the Lock Task allowlist and restores global actions. Validate this behavior on an organization-provisioned test device before deployment.

## Tech Stack
- **Language**: Kotlin 2.1+
- **UI**: Jetpack Compose + Material 3
- **Build System**: Gradle with Kotlin DSL (`*.gradle.kts`)
- **Architecture**: Single module (`:app`), unidirectional data flow, clean separation of concerns.

## Development & AI Rules
All development follows strict guidelines documented in `.ai/RULES.md`. Current progress is tracked in `.ai/CURRENT_STATE.md` and `.ai/SESSION_LOG.md`.
