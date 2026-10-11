package com.eonmirth.panelguard.platform

import android.app.KeyguardManager
import android.content.Context

interface LockStateReader {
    /** True if the device currently has a secure lock method (PIN, pattern, password, biometric). */
    fun isDeviceSecure(): Boolean

    /** True if the keyguard is currently locked. */
    fun isKeyguardLocked(): Boolean
}

class AndroidLockStateReader(private val context: Context) : LockStateReader {

    private val keyguardManager: KeyguardManager? by lazy {
        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }

    override fun isDeviceSecure(): Boolean {
        return keyguardManager?.isDeviceSecure == true
    }

    override fun isKeyguardLocked(): Boolean {
        return keyguardManager?.isKeyguardLocked == true
    }
}
