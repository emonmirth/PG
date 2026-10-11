package com.eonmirth.panelguard.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Palette (Material 3)
val md_theme_light_primary = Color(0xFF1E56A0)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFD6E4FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001B3D)

val md_theme_light_secondary = Color(0xFF163172)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD9E2FF)
val md_theme_light_onSecondaryContainer = Color(0xFF001945)

val md_theme_light_tertiary = Color(0xFF006874)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFF97F0FF)
val md_theme_light_onTertiaryContainer = Color(0xFF001F24)

val md_theme_light_background = Color(0xFFF8F9FA)
val md_theme_light_onBackground = Color(0xFF1A1C1E)
val md_theme_light_surface = Color(0xFFFFFFFF)
val md_theme_light_onSurface = Color(0xFF1A1C1E)
val md_theme_light_surfaceVariant = Color(0xFFE0E2EC)
val md_theme_light_onSurfaceVariant = Color(0xFF43474E)
val md_theme_light_outline = Color(0xFF74777F)

// Dark Theme Palette (Material 3)
val md_theme_dark_primary = Color(0xFFA9C7FF)
val md_theme_dark_onPrimary = Color(0xFF003063)
val md_theme_dark_primaryContainer = Color(0xFF00468A)
val md_theme_dark_onPrimaryContainer = Color(0xFFD6E4FF)

val md_theme_dark_secondary = Color(0xFFB0C6FF)
val md_theme_dark_onSecondary = Color(0xFF102D60)
val md_theme_dark_secondaryContainer = Color(0xFF2B4478)
val md_theme_dark_onSecondaryContainer = Color(0xFFD9E2FF)

val md_theme_dark_tertiary = Color(0xFF4FD8EB)
val md_theme_dark_onTertiary = Color(0xFF00363D)
val md_theme_dark_tertiaryContainer = Color(0xFF004F58)
val md_theme_dark_onTertiaryContainer = Color(0xFF97F0FF)

val md_theme_dark_background = Color(0xFF0F1216)
val md_theme_dark_onBackground = Color(0xFFE2E2E6)
val md_theme_dark_surface = Color(0xFF171A1F)
val md_theme_dark_onSurface = Color(0xFFE2E2E6)
val md_theme_dark_surfaceVariant = Color(0xFF262A32)
val md_theme_dark_onSurfaceVariant = Color(0xFFC4C6CF)
val md_theme_dark_outline = Color(0xFF8E9099)

// Theme-Aware Status Colors
data class StatusColorPair(
    val containerColor: Color,
    val contentColor: Color
)

fun getActiveStatusColors(isDark: Boolean): StatusColorPair = if (isDark) {
    StatusColorPair(Color(0xFF163223), Color(0xFF81C784))
} else {
    StatusColorPair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
}

fun getPartialStatusColors(isDark: Boolean): StatusColorPair = if (isDark) {
    StatusColorPair(Color(0xFF3B2F10), Color(0xFFFFD54F))
} else {
    StatusColorPair(Color(0xFFFFF8E1), Color(0xFFF57F17))
}

fun getInactiveStatusColors(isDark: Boolean): StatusColorPair = if (isDark) {
    StatusColorPair(Color(0xFF3A1C1C), Color(0xFFE57373))
} else {
    StatusColorPair(Color(0xFFFFEBEE), Color(0xFFC62828))
}

fun getManagedStatusColors(isDark: Boolean): StatusColorPair = if (isDark) {
    StatusColorPair(Color(0xFF182B4A), Color(0xFF64B5F6))
} else {
    StatusColorPair(Color(0xFFE3F2FD), Color(0xFF1565C0))
}

fun getNeutralStatusColors(isDark: Boolean): StatusColorPair = if (isDark) {
    StatusColorPair(Color(0xFF272A30), Color(0xFFB0BEC5))
} else {
    StatusColorPair(Color(0xFFECEFF1), Color(0xFF546E7A))
}
