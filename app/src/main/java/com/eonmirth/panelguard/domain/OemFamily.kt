package com.eonmirth.panelguard.domain

enum class OemFamily(
    val displayName: String,
    val hasKnownLockScreenShadeSetting: Boolean
) {
    SAMSUNG(
        displayName = "Samsung (One UI)",
        hasKnownLockScreenShadeSetting = true
    ),
    XIAOMI(
        displayName = "Xiaomi / POCO / Redmi (HyperOS / MIUI)",
        hasKnownLockScreenShadeSetting = true
    ),
    OPPO(
        displayName = "OPPO / Realme (ColorOS / Realme UI)",
        hasKnownLockScreenShadeSetting = true
    ),
    ONEPLUS(
        displayName = "OnePlus (OxygenOS)",
        hasKnownLockScreenShadeSetting = true
    ),
    VIVO(
        displayName = "vivo / iQOO (Funtouch / OriginOS)",
        hasKnownLockScreenShadeSetting = true
    ),
    GOOGLE(
        displayName = "Google Pixel (Stock Android)",
        hasKnownLockScreenShadeSetting = false
    ),
    MOTOROLA(
        displayName = "Motorola (My UX / Hello UI)",
        hasKnownLockScreenShadeSetting = false
    ),
    GENERIC(
        displayName = "Generic / Other Android Device",
        hasKnownLockScreenShadeSetting = false
    );

    fun getGuidanceSteps(): List<String> = when (this) {
        SAMSUNG -> listOf(
            "Open Settings > Lock screen",
            "Tap 'Secure lock settings'",
            "Turn ON 'Lock network and security' so Wi-Fi and mobile data cannot be toggled from the lock screen"
        )
        XIAOMI -> listOf(
            "Open Settings > Notifications & Control center",
            "Tap 'Items shown on the Lock screen after swiping down'",
            "Turn OFF 'Control center' and 'Notification shade'"
        )
        OPPO, ONEPLUS -> listOf(
            "Open Settings > Notification & status bar",
            "Tap 'Lock screen'",
            "Turn OFF 'Swipe down on Lock screen to view notification drawer'"
        )
        VIVO -> listOf(
            "Open Settings > Lock screen & wallpaper",
            "Tap 'Lock screen settings'",
            "Turn OFF 'Open Control Center on the lock screen'"
        )
        GOOGLE -> listOf(
            "Stock Android does not provide a direct third-party consumer toggle for the notification shade.",
            "Recommendation: Enable 'Lockdown' option in Settings > Display > Lock screen to disable Smart Lock, biometrics, and notifications on demand.",
            "Enterprise alternative: Provision this device as a Managed Device (Device Owner)."
        )
        MOTOROLA -> listOf(
            "Stock-based Android skin does not offer a direct shade toggle.",
            "Recommendation: Enable system Lockdown mode and set sensitive notifications to hidden.",
            "Enterprise alternative: Provision as a Managed Device (Device Owner)."
        )
        GENERIC -> listOf(
            "Open Settings > Security or Lock screen to inspect vendor-specific shade options.",
            "If unavailable, rely on system Lockdown mode or Managed Device (Device Owner) provisioning."
        )
    }

    companion object {
        fun detect(manufacturer: String?, brand: String? = null): OemFamily {
            val combined = "${manufacturer.orEmpty()} ${brand.orEmpty()}".lowercase().trim()
            if (combined.isEmpty()) return GENERIC

            return when {
                combined.contains("samsung") -> SAMSUNG
                combined.contains("xiaomi") || combined.contains("redmi") || combined.contains("poco") -> XIAOMI
                combined.contains("oppo") || combined.contains("realme") -> OPPO
                combined.contains("oneplus") -> ONEPLUS
                combined.contains("vivo") || combined.contains("iqoo") -> VIVO
                combined.contains("google") -> GOOGLE
                combined.contains("motorola") || combined.contains("lenovo") || combined.contains("moto") -> MOTOROLA
                else -> GENERIC
            }
        }
    }
}
