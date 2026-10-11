package com.eonmirth.panelguard.platform

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.eonmirth.panelguard.domain.OemFamily

interface SettingsNavigator {
    fun canResolve(intent: Intent): Boolean
    fun getOemSettingIntent(oemFamily: OemFamily): Intent?
    fun getSecuritySettingsIntent(): Intent
    fun getGeneralSettingsIntent(): Intent
}

class AndroidSettingsNavigator(private val context: Context) : SettingsNavigator {

    override fun canResolve(intent: Intent): Boolean {
        val resolveInfo = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return resolveInfo != null
    }

    override fun getOemSettingIntent(oemFamily: OemFamily): Intent? {
        val candidates = when (oemFamily) {
            OemFamily.SAMSUNG -> listOf(
                Intent("android.settings.LOCK_SCREEN_SETTINGS"),
                Intent(Settings.ACTION_SECURITY_SETTINGS)
            )
            OemFamily.XIAOMI -> listOf(
                Intent("android.settings.NOTIFICATION_SETTINGS"),
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                },
                Intent(Settings.ACTION_SECURITY_SETTINGS)
            )
            OemFamily.OPPO, OemFamily.ONEPLUS -> listOf(
                Intent("android.settings.NOTIFICATION_SETTINGS"),
                Intent(Settings.ACTION_SECURITY_SETTINGS)
            )
            OemFamily.VIVO -> listOf(
                Intent(Settings.ACTION_SECURITY_SETTINGS),
                Intent(Settings.ACTION_SETTINGS)
            )
            OemFamily.GOOGLE, OemFamily.MOTOROLA, OemFamily.GENERIC -> listOf(
                Intent(Settings.ACTION_SECURITY_SETTINGS),
                Intent(Settings.ACTION_SETTINGS)
            )
        }

        return candidates.firstOrNull { canResolve(it) }
    }

    override fun getSecuritySettingsIntent(): Intent {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        return if (canResolve(intent)) intent else getGeneralSettingsIntent()
    }

    override fun getGeneralSettingsIntent(): Intent {
        return Intent(Settings.ACTION_SETTINGS)
    }
}
