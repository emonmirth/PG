# Internal Release Checklist

- [ ] All unit tests pass (`./gradlew test`)
- [ ] No hidden API usage or prohibited reflection libraries
- [ ] No AccessibilityService shade interceptor hacks
- [ ] No deceptive overlays or anti-uninstall locks
- [ ] Device Owner mode strictly gated and distinguished from consumer mode
- [ ] Recovery flow validated
- [ ] OEM guidance verified across at least 1 physical OEM device
- [ ] APK built cleanly with debug/release signing
