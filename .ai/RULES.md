# AI AGENT RULES — EonMirth Panel Guard

## Mission
Build and maintain a real Android security utility focused on lock-screen notification shade / Quick Settings protection.

## Non-negotiable
1. Never invent an Android API.
2. Never claim a feature works before testing it.
3. Never silently weaken security behavior.
4. Never use hidden APIs, HiddenApiBypass, root, SystemUI modification, or deceptive overlays.
5. Never use AccessibilityService merely to dismiss or chase the notification shade.
6. Device Owner / Lock Task is a managed-device capability, not a normal-user shortcut.
7. Prefer official Android SDK / Jetpack APIs.
8. If platform behavior is uncertain, mark it UNKNOWN and verify with official docs or a reproducible test.
9. Do not add a dependency unless its need is clear.
10. Keep the project buildable after every meaningful change.
11. Do not rewrite working code just because a different architecture looks nicer.
12. Do not change product scope without explicit approval.
13. Do not create fake test results.
14. Do not say “tested” unless the command/device/emulator actually ran.
15. Keep a changelog and current-state file after each completed phase.

## Agent workflow
READ -> PLAN -> INSPECT -> CHANGE -> TEST -> REVIEW -> DOCUMENT -> STOP

Before coding:
- Read .ai/CURRENT_STATE.md
- Read .ai/NEXT_TASK.md
- Read docs/PRODUCT_SPEC.md
- Read docs/PLATFORM_LIMITS.md
- Inspect the actual repository.
- Identify the smallest correct change.

After coding:
- Run the relevant tests.
- Run lint/static checks where available.
- Build the affected variant.
- Inspect errors instead of guessing.
- Update .ai/CURRENT_STATE.md and .ai/SESSION_LOG.md.
- Update docs/CHANGELOG.md if behavior changed.
- Write exactly one next recommended task.

## Security
- No credential collection.
- No stealth.
- No surveillance.
- No anti-uninstall.
- No hidden persistence.
- No bypass of Android security boundaries.
- No user lockout.
- Always provide a recovery path.

## Truthfulness
Every capability must have one of:
- SUPPORTED
- PARTIAL
- MANAGED_ONLY
- OEM_GUIDANCE
- UNSUPPORTED
- UNKNOWN

Never convert UNKNOWN into SUPPORTED by assumption.
