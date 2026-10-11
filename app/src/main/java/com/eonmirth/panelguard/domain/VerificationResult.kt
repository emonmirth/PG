package com.eonmirth.panelguard.domain

/**
 * The result of a real-state verification pass.
 *
 * Every field that describes "protection is X" MUST be backed by a concrete, machine-readable
 * evidence signal — never inferred from UI events such as opening a settings screen or clicking
 * a button. [reason] documents exactly which evidence signal led to this result.
 */
enum class VerificationStatus {
    /** At least one concrete, verified protection mechanism is active. */
    ACTIVE,

    /**
     * A protection mechanism is available and partially set up but cannot be confirmed active
     * without the user completing a manual step (OEM toggle) that we cannot read back.
     */
    PARTIAL,

    /** No protection has been configured. Setup is required before any protection is active. */
    SETUP_REQUIRED,

    /** Protection is active because this app is a Device Owner with Lock Task configured. */
    MANAGED_ONLY,

    /**
     * The hardware / OS combination does not expose any capability we can use or guide
     * through to achieve protection.
     */
    UNSUPPORTED,

    /**
     * We do not have enough information to decide. This is the honest answer when the OEM
     * family is unrecognised or evidence signals are conflicting.
     */
    UNKNOWN,

    /** An unexpected error occurred during evaluation. [reason] contains the exception message. */
    ERROR
}

/**
 * Full verification result returned by [VerificationEvaluator].
 *
 * @param status        The determined protection status.
 * @param title         A one-line human-readable headline for the UI.
 * @param summary       Two-to-three sentence elaboration shown beneath the title.
 * @param reason        Precise machine-derived evidence that led to [status]. Never inferential.
 * @param actionLabel   Optional CTA text. Null when no user action is needed.
 * @param capabilities  The raw capability list used as input evidence.
 */
data class VerificationResult(
    val status: VerificationStatus,
    val title: String,
    val summary: String,
    val reason: String,
    val actionLabel: String? = null,
    val capabilities: List<Capability> = emptyList()
) {
    val isProtected: Boolean
        get() = status == VerificationStatus.ACTIVE || status == VerificationStatus.MANAGED_ONLY
}
