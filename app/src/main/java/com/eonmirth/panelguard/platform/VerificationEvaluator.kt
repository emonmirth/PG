package com.eonmirth.panelguard.platform

import com.eonmirth.panelguard.domain.Capability
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.VerificationResult
import com.eonmirth.panelguard.domain.VerificationStatus

/**
 * Phase 4 — Real-state evaluator.
 *
 * Rules (MUST be respected):
 * - Every [VerificationResult] MUST name a concrete, machine-readable evidence signal.
 * - Never infer protection from: a settings intent being launched, a button being tapped,
 *   or a permission existing in the manifest.
 * - User-confirmed OEM toggles are accepted ONLY as [VerificationStatus.PARTIAL] because we
 *   cannot read OEM proprietary settings back programmatically.
 * - [VerificationStatus.ACTIVE] is reserved for states we can independently verify:
 *     • Device Owner + Lock Task permitted + lock screen secure.
 *     • (Future: verified OEM setting via content-provider or secure-settings read.)
 * - [VerificationStatus.ERROR] is emitted when an unexpected exception escapes the evaluator.
 */
class VerificationEvaluator(
    private val devicePolicyController: DevicePolicyController,
    private val lockStateReader: LockStateReader,
    private val oemCapabilityReader: OemCapabilityReader
) {

    fun evaluate(
        oemFamily: OemFamily,
        sdkInt: Int,
        userConfirmedOemSetting: Boolean = false
    ): VerificationResult {
        return try {
            evaluateInternal(oemFamily, sdkInt, userConfirmedOemSetting)
        } catch (e: Exception) {
            VerificationResult(
                status = VerificationStatus.ERROR,
                title = "Verification Error",
                summary = "An unexpected error occurred while reading device state. " +
                    "The protection status is unknown.",
                reason = "Exception during evaluation: ${e.javaClass.simpleName}: ${e.message}",
                actionLabel = "Retry"
            )
        }
    }

    private fun evaluateInternal(
        oemFamily: OemFamily,
        sdkInt: Int,
        userConfirmedOemSetting: Boolean
    ): VerificationResult {

        val capabilities = oemCapabilityReader.evaluateCapabilities(oemFamily, sdkInt)

        // ── Evidence signal 1: Device Owner + Lock Task ──────────────────────────────
        val isDeviceOwner = devicePolicyController.isDeviceOwner()
        if (isDeviceOwner) {
            val lockTaskPermitted = devicePolicyController.isLockTaskPermitted()
            val isSecure = lockStateReader.isDeviceSecure()

            return if (lockTaskPermitted && isSecure) {
                VerificationResult(
                    status = VerificationStatus.MANAGED_ONLY,
                    title = "Enterprise Protection Active",
                    summary = "This device is enrolled as Device Owner. Lock Task mode is " +
                        "configured, which prevents access to the notification shade and Quick " +
                        "Settings. Protection is enforced at the OS level.",
                    reason = "Evidence: DevicePolicyManager.isDeviceOwnerApp=true, " +
                        "isLockTaskPermitted=true, KeyguardManager.isDeviceSecure=true.",
                    capabilities = capabilities
                )
            } else if (lockTaskPermitted && !isSecure) {
                VerificationResult(
                    status = VerificationStatus.PARTIAL,
                    title = "Lock Task Active — No Screen Lock",
                    summary = "Lock Task is configured, but the device does not have a secure " +
                        "lock screen credential. An attacker could bypass the lock screen " +
                        "without a PIN or password.",
                    reason = "Evidence: isDeviceOwnerApp=true, isLockTaskPermitted=true, " +
                        "KeyguardManager.isDeviceSecure=false.",
                    actionLabel = "Configure Screen Lock",
                    capabilities = capabilities
                )
            } else {
                VerificationResult(
                    status = VerificationStatus.SETUP_REQUIRED,
                    title = "Device Owner Enrolled — Lock Task Not Configured",
                    summary = "This device is enrolled as a Device Owner, which enables " +
                        "enterprise-grade protection. However, Lock Task mode has not yet been " +
                        "configured. Enable managed Lock Task in the app to activate protection.",
                    reason = "Evidence: isDeviceOwnerApp=true, isLockTaskPermitted=false.",
                    actionLabel = "Enable Managed Lock Task",
                    capabilities = capabilities
                )
            }
        }

        // ── Evidence signal 2: Secure lock screen credential ─────────────────────────
        val isDeviceSecure = lockStateReader.isDeviceSecure()
        if (!isDeviceSecure) {
            return VerificationResult(
                status = VerificationStatus.SETUP_REQUIRED,
                title = "No Secure Lock Screen",
                summary = "The device does not have a PIN, pattern, or password configured. " +
                    "Lock-screen protection is meaningless without a secure credential, " +
                    "because anyone can reach the home screen without authentication.",
                reason = "Evidence: KeyguardManager.isDeviceSecure=false.",
                actionLabel = "Set Up Screen Lock",
                capabilities = capabilities
            )
        }

        // ── Evidence signal 3: OEM family with known shade setting ───────────────────
        if (oemFamily.hasKnownLockScreenShadeSetting) {
            // We CANNOT programmatically read OEM proprietary secure-settings keys.
            // We accept user self-attestation as PARTIAL, never ACTIVE.
            return if (userConfirmedOemSetting) {
                VerificationResult(
                    status = VerificationStatus.PARTIAL,
                    title = "OEM Setting Enabled (User-Confirmed)",
                    summary = "${oemFamily.displayName} provides an official system setting to " +
                        "disable the notification shade on the lock screen. You confirmed this " +
                        "setting is active. Note: we cannot independently verify OEM proprietary " +
                        "settings — please re-confirm if the device is reset or updated.",
                    reason = "Evidence: OemFamily=${oemFamily.name}, " +
                        "hasKnownLockScreenShadeSetting=true, " +
                        "KeyguardManager.isDeviceSecure=true, " +
                        "userConfirmedOemSetting=true (self-attestation, not machine-verified).",
                    capabilities = capabilities
                )
            } else {
                VerificationResult(
                    status = VerificationStatus.SETUP_REQUIRED,
                    title = "OEM Setting Available — Not Confirmed",
                    summary = "${oemFamily.displayName} includes a system setting that can " +
                        "prevent notification shade access from the lock screen. Follow the " +
                        "setup guide and confirm the setting is active.",
                    reason = "Evidence: OemFamily=${oemFamily.name}, " +
                        "hasKnownLockScreenShadeSetting=true, " +
                        "KeyguardManager.isDeviceSecure=true, " +
                        "userConfirmedOemSetting=false.",
                    actionLabel = "Open Setup Guide",
                    capabilities = capabilities
                )
            }
        }

        // ── Evidence signal 4: Stock AOSP / Motorola — no consumer toggle ────────────
        if (oemFamily == OemFamily.GOOGLE || oemFamily == OemFamily.MOTOROLA) {
            return VerificationResult(
                status = VerificationStatus.UNSUPPORTED,
                title = "Consumer Toggle Not Available",
                summary = "Stock Android and Motorola devices do not expose a public API or " +
                    "system setting for third-party apps to disable the notification shade on " +
                    "the lock screen. Use Android Lockdown Mode for on-demand restriction, " +
                    "or enrol as a managed Device Owner for enterprise control.",
                reason = "Evidence: OemFamily=${oemFamily.name}, " +
                    "AOSP SystemUI does not expose com.android.systemui shade-on-lock-screen " +
                    "secure settings; hasKnownLockScreenShadeSetting=false.",
                capabilities = capabilities
            )
        }

        // ── Fallback: unrecognised OEM ────────────────────────────────────────────────
        return VerificationResult(
            status = VerificationStatus.UNKNOWN,
            title = "Device Capability Unknown",
            summary = "The device vendor is not in the verified OEM support list. We cannot " +
                "confirm whether a native shade-restriction setting exists. Inspect the device " +
                "security and display settings manually.",
            reason = "Evidence: OemFamily=${oemFamily.name}, " +
                "hasKnownLockScreenShadeSetting=false, " +
                "isDeviceOwner=false, isDeviceSecure=true.",
            actionLabel = "View Manual Guide",
            capabilities = capabilities
        )
    }
}
