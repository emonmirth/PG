# KNOWN ISSUES

## Platform & OEM Limitations
1. **Third-Party App Sandboxing**: Android SystemUI is owned by the OS and OEM. Standard third-party apps cannot programmatically toggle or directly disable notification shade / Quick Settings on the lock screen without user interaction or managed device status.
2. **OEM Setting Fragmentation**: Settings paths for disabling lock screen shade pull-down vary widely between Samsung One UI, Xiaomi HyperOS, OPPO ColorOS, OnePlus OxygenOS, and stock Android.
3. **Managed Device Prerequisite**: Lock Task mode with `LOCK_TASK_FEATURE_NONE` strictly requires Device Owner provisioning, which is enterprise/dedicated device functionality and cannot be self-granted by a consumer app.
4. **Intent Navigation Verification**: Launching a system Settings intent successfully does not guarantee the user actually toggled the setting. Observability or explicit confirmation is required.
