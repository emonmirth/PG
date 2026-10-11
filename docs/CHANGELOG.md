# Changelog

All notable changes to EonMirth Panel Guard will be documented in this file.

## [Unreleased]
### Added
- **Phase 0**: Project bootstrap, `.ai/` and `docs/` control plane established, Kotlin DSL + Compose build setup.
- **Phase 1**: Capability Engine with `Capability`, `OemFamily`, `ProtectionState`, `DevicePolicyController`, `LockStateReader`, `SettingsNavigator`, `OemCapabilityReader`.
- **Phase 2**: Consumer Setup / OEM Guidance with `CompatibilityScreen`, `SetupScreen`, `AppRoot`, verified OEM instructions, user confirmation state handling, and 22 unit tests.
- **Phase 3**: Managed Device / Device Owner with protected admin receiver registration, Device Owner-gated Lock Task configuration, explicit confirmation, in-app recovery, and non-owner unit coverage.
- **Phase 4**: Verification Screen & Real-State Evaluator (`VerificationScreen`, `VerificationEvaluator`, evidence-based truth evaluation, 36/36 unit tests passing).
- **Phase 5**: UI Polish, Dark Theme Aesthetic & Material 3 Refinement (`PanelGuardTheme`, `Color.kt`, theme-aware status badges, Compose `@Preview`s on all screens).
