# Platform Limitations: Android Lock-Screen & SystemUI

## 1. SystemUI Ownership
On Android, the notification shade and Quick Settings panel are rendered by `com.android.systemui`, an OS-privileged process. Normal sandboxed applications (`UID >= 10000`) have no direct API access to disable or restrict SystemUI pull-down events.

## 2. Lock Task Mode (`DevicePolicyManager`)
- `LOCK_TASK_FEATURE_NONE` disables configurable Lock Task system UI features, including notifications, Quick Settings, Home, Recents, and the power-actions dialog.
- **Limitation**: Only available if the app is provisioned as **Device Owner** (or whitelisted by a Device Owner). This requires device factory reset / NFC / QR provisioning or ADB command setup. It cannot be enabled by a consumer clicking a button.
- **Recovery**: This app exposes an in-mode exit action that stops Lock Task, removes this app from the Lock Task allowlist, and restores global actions. Administrators should validate recovery on their managed-device deployment before rollout.

## 3. OEM System Settings
Some OEMs provide native toggles to disable notification center / control center on the lock screen:
- **Samsung One UI**: "Lock screen > Secure lock settings > Lock network and security".
- **Xiaomi MIUI / HyperOS**: "Notifications & Control Center > Items shown on the Lock screen after swiping down > Control center".
- **OPPO ColorOS / OnePlus OxygenOS**: "Notification & Status Bar > Lock screen > Swipe down on Lock screen to view notification drawer".
- **Stock Android / Pixel**: Limited native control without Device Owner or full lockdown mode.

## 4. Prohibited Techniques
- Chasing shade open events using `AccessibilityService.performGlobalAction()` (unreliable race condition, battery drain, Play Store policy violation).
- Hidden API reflection (`HiddenApiBypass`).
- Fake system alert overlays (`TYPE_APPLICATION_OVERLAY`) on lock screen.
