package com.eonmirth.panelguard.domain

enum class ProtectionMode {
    NONE,
    OEM_SETTING,
    MANAGED_LOCK_TASK
}

enum class ProtectionLevel {
    NOT_ACTIVE,
    PARTIAL,
    ACTIVE,
    MANAGED_ENFORCED,
    UNSUPPORTED,
    UNKNOWN
}

data class ProtectionState(
    val level: ProtectionLevel,
    val mode: ProtectionMode,
    val summary: String,
    val verifiedReason: String? = null,
    val capabilities: List<Capability> = emptyList()
) {
    val isProtected: Boolean
        get() = level == ProtectionLevel.ACTIVE || level == ProtectionLevel.MANAGED_ENFORCED

    companion object {
        fun initial(): ProtectionState = ProtectionState(
            level = ProtectionLevel.NOT_ACTIVE,
            mode = ProtectionMode.NONE,
            summary = "Protection not evaluated yet"
        )
    }
}
