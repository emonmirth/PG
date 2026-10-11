package com.eonmirth.panelguard.domain

/**
 * Truthful representation of what a specific device, OEM, or deployment mode supports.
 * Never converts UNKNOWN into SUPPORTED by assumption.
 */
enum class CapabilityStatus {
    /** Direct public platform feature is verified and usable on this device. */
    SUPPORTED,

    /** Partial mitigation available (e.g., Lockdown mode, restricted lock screen widgets). */
    PARTIAL,

    /** Supported ONLY when the app is explicitly provisioned as Device Owner (managed enterprise). */
    MANAGED_ONLY,

    /** Supported via an OEM-specific user toggle in system settings. */
    OEM_GUIDANCE,

    /** The platform and OEM do not offer third-party shade restriction on this device. */
    UNSUPPORTED,

    /** Insufficient telemetry or unverified vendor/build. Never assumed to be SUPPORTED. */
    UNKNOWN
}

data class Capability(
    val id: String,
    val title: String,
    val status: CapabilityStatus,
    val description: String,
    val recommendation: String,
    val confidenceReason: String
)
