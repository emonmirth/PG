# Test Plan: EonMirth Panel Guard

## 1. Unit Testing Strategy
- **Capability Logic**: Verify manufacturer string parsing, OS version branching, and device owner status transitions.
- **State Evaluation**: Verify every `ProtectionState` evaluation accurately reflects known inputs (never assuming ACTIVE).
- **Intent Safety**: Ensure all intent constructions handle missing activity exceptions gracefully.

## 2. Integration / Device Testing Matrix
- Test on Android Emulator (API 34/35/36).
- Test on physical Samsung One UI device.
- Test on physical Xiaomi HyperOS device.
- Test on physical Google Pixel / Stock Android device.
- Record actual observations in `docs/DEVICE_TEST_MATRIX.md`.
