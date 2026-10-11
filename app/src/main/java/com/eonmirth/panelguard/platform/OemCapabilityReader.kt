package com.eonmirth.panelguard.platform

import com.eonmirth.panelguard.domain.Capability
import com.eonmirth.panelguard.domain.CapabilityStatus
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.ProtectionLevel
import com.eonmirth.panelguard.domain.ProtectionMode
import com.eonmirth.panelguard.domain.ProtectionState

class OemCapabilityReader(
    private val devicePolicyController: DevicePolicyController,
    private val lockStateReader: LockStateReader
) {

    fun evaluateCapabilities(
        oemFamily: OemFamily,
        sdkInt: Int
    ): List<Capability> {
        val list = mutableListOf<Capability>()

        // 1. Secure Credential Check
        val isSecure = lockStateReader.isDeviceSecure()
        list.add(
            Capability(
                id = "cap_screen_lock",
                title = "Secure Lock Screen Credential",
                status = if (isSecure) CapabilityStatus.SUPPORTED else CapabilityStatus.PARTIAL,
                description = if (isSecure) {
                    "The device has a verified secure lock (PIN, pattern, password, or biometric)."
                } else {
                    "No secure lock is configured. Lock-screen security requires a secure credential."
                },
                recommendation = if (isSecure) {
                    "Maintain secure screen lock credentials."
                } else {
                    "Configure a PIN, pattern, or password in system Security settings."
                },
                confidenceReason = "Verified directly via KeyguardManager.isDeviceSecure."
            )
        )

        // 2. OEM-specific Notification Shade / Quick Settings Setting
        val oemCap = when {
            oemFamily.hasKnownLockScreenShadeSetting -> Capability(
                id = "cap_oem_shade_toggle",
                title = "OEM Lock-Screen Shade Setting",
                status = CapabilityStatus.OEM_GUIDANCE,
                description = "${oemFamily.displayName} provides an official system setting to disable notification shade or quick settings on the lock screen.",
                recommendation = "Follow the setup guide to toggle the setting in system settings.",
                confidenceReason = "Verified against known OEM feature support matrix."
            )
            oemFamily == OemFamily.GOOGLE || oemFamily == OemFamily.MOTOROLA -> Capability(
                id = "cap_oem_shade_toggle",
                title = "Stock Lock-Screen Behavior",
                status = CapabilityStatus.PARTIAL,
                description = "Stock Android does not provide a direct third-party consumer toggle to disable the notification shade.",
                recommendation = "Use Android Lockdown mode to restrict notifications on demand, or enroll as a Managed Device.",
                confidenceReason = "AOSP SystemUI architectural design boundary."
            )
            else -> Capability(
                id = "cap_oem_shade_toggle",
                title = "OEM Shade Customization",
                status = CapabilityStatus.UNKNOWN,
                description = "Vendor-specific lock-screen shade setting availability is unverified for this device build.",
                recommendation = "Inspect device display and security settings manually.",
                confidenceReason = "Unknown OEM family; cannot confirm native setting without device test."
            )
        }
        list.add(oemCap)

        // 3. Managed Device / Device Owner Lock Task Path
        val isDeviceOwner = devicePolicyController.isDeviceOwner()
        val managedCap = if (isDeviceOwner) {
            Capability(
                id = "cap_managed_lock_task",
                title = "Enterprise Lock Task Protection",
                status = CapabilityStatus.SUPPORTED,
                description = "This device is provisioned as Device Owner. SystemUI features can be strictly disabled via LOCK_TASK_FEATURE_NONE.",
                recommendation = "Enable managed Lock Task mode when running in dedicated kiosk or high-security mode.",
                confidenceReason = "DevicePolicyManager.isDeviceOwnerApp returned true."
            )
        } else {
            Capability(
                id = "cap_managed_lock_task",
                title = "Enterprise Lock Task Protection",
                status = CapabilityStatus.MANAGED_ONLY,
                description = "DevicePolicyManager Lock Task requires enterprise Device Owner provisioning. Normal consumer apps cannot activate this mode.",
                recommendation = "Consumer devices should use the OEM Guidance path instead.",
                confidenceReason = "DevicePolicyManager.isDeviceOwnerApp returned false. Device Owner cannot be granted without enterprise provisioning."
            )
        }
        list.add(managedCap)

        return list
    }

    fun evaluateProtectionState(
        oemFamily: OemFamily,
        sdkInt: Int,
        userConfirmedOemSetting: Boolean = false
    ): ProtectionState {
        val capabilities = evaluateCapabilities(oemFamily, sdkInt)
        val isDeviceOwner = devicePolicyController.isDeviceOwner()
        val isDeviceSecure = lockStateReader.isDeviceSecure()

        if (isDeviceOwner) {
            val isLockTaskPermitted = devicePolicyController.isLockTaskPermitted()
            return if (isLockTaskPermitted) {
                ProtectionState(
                    level = ProtectionLevel.MANAGED_ENFORCED,
                    mode = ProtectionMode.MANAGED_LOCK_TASK,
                    summary = "Enterprise Managed Lock Task protection active",
                    verifiedReason = "Device Owner confirmed and Lock Task mode is permitted.",
                    capabilities = capabilities
                )
            } else {
                ProtectionState(
                    level = ProtectionLevel.PARTIAL,
                    mode = ProtectionMode.NONE,
                    summary = "Device Owner enrolled, but Lock Task is not configured",
                    verifiedReason = "App is Device Owner but Lock Task is not yet permitted.",
                    capabilities = capabilities
                )
            }
        }

        if (!isDeviceSecure) {
            return ProtectionState(
                level = ProtectionLevel.NOT_ACTIVE,
                mode = ProtectionMode.NONE,
                summary = "Lock screen credential not configured",
                verifiedReason = "Device is not secured with PIN/pattern/password.",
                capabilities = capabilities
            )
        }

        if (oemFamily.hasKnownLockScreenShadeSetting) {
            return if (userConfirmedOemSetting) {
                ProtectionState(
                    level = ProtectionLevel.ACTIVE,
                    mode = ProtectionMode.OEM_SETTING,
                    summary = "OEM lock-screen shade protection confirmed",
                    verifiedReason = "User verified OEM setting is active on ${oemFamily.displayName}.",
                    capabilities = capabilities
                )
            } else {
                ProtectionState(
                    level = ProtectionLevel.PARTIAL,
                    mode = ProtectionMode.NONE,
                    summary = "OEM setting available — Setup required",
                    verifiedReason = "${oemFamily.displayName} supports lock-screen shade protection, awaiting user setup.",
                    capabilities = capabilities
                )
            }
        }

        if (oemFamily == OemFamily.GOOGLE || oemFamily == OemFamily.MOTOROLA) {
            return ProtectionState(
                level = ProtectionLevel.PARTIAL,
                mode = ProtectionMode.NONE,
                summary = "Consumer shade toggle not supported by stock Android",
                verifiedReason = "Stock AOSP does not expose consumer shade restriction. Lockdown mode advised.",
                capabilities = capabilities
            )
        }

        return ProtectionState(
            level = ProtectionLevel.UNKNOWN,
            mode = ProtectionMode.NONE,
            summary = "Device capability status is UNKNOWN",
            verifiedReason = "Unverified vendor/OEM build. Cannot assume protection without test.",
            capabilities = capabilities
        )
    }
}
