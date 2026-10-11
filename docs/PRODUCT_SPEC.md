# Product Specification: EonMirth Panel Guard

## 1. Product Summary
- **Product Name**: EonMirth Panel Guard
- **Package Name**: `com.eonmirth.panelguard`
- **Core Promise**: "Help protect the lock screen from unwanted notification-shade / Quick Settings access where your device supports it."
- **Target Platform**: Android 10+ (API 29+), targeting modern Android versions (API 34–36+).

## 2. Core User Flows
1. **Launch & Diagnostics**: User opens app; immediate capability detection runs.
2. **Compatibility Presentation**: App reveals whether lock-screen panel protection is supported natively, via OEM settings guidance, via Managed Device mode, or unsupported.
3. **Setup Guidance**: Clear, step-by-step instructions or direct deep links to OEM system settings.
4. **Verification**: Checks actual observable conditions and provides truthful feedback (Active, Partial, Setup Required, Managed Only, Unsupported).
5. **Safe Recovery**: Plain instructions on how to revert settings and restore default behavior.

## 3. Strict Safety Boundaries
- No accessibility race-condition shade catchers.
- No root or hidden API bypass.
- No fake screen overlays or anti-uninstall locks.
- No deceptive claims of "100% theft protection" or "impossible to turn off".
