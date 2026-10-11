# Architecture & Product Decision Records

## ADR 001: Prohibition of Accessibility Race Condition Blocker
- **Context**: Many low-grade utilities attempt to detect `TYPE_WINDOW_STATE_CHANGED` for SystemUI shade and immediately trigger `performGlobalAction(GLOBAL_ACTION_BACK)` or `GLOBAL_ACTION_HOME`.
- **Decision**: Strictly prohibited.
- **Rationale**: This is a known race condition; the shade briefly flashes open, allows quick taps (e.g. airplane mode toggle), consumes battery, and violates Google Play policies regarding accessibility service abuse.

## ADR 002: Managed Device Separation
- **Context**: Android DevicePolicyManager provides enterprise-grade lock task mode.
- **Decision**: Treat Device Owner as an explicit managed-device mode, completely isolated from standard consumer setup flows.
- **Rationale**: Prevents misleading ordinary users into believing they can click a button to lock down system controls without enterprise provisioning.

## ADR 003: Single-Module Kotlin DSL + Compose Architecture
- **Context**: App needs to be maintainable, modern, and rapid to iterate on.
- **Decision**: Standard single-module (`:app`) project with Jetpack Compose Material 3 and Kotlin DSL (`build.gradle.kts`).
- **Rationale**: Eliminates multi-module overhead while preserving clean separation between `domain`, `platform`, `data`, and `ui`.
