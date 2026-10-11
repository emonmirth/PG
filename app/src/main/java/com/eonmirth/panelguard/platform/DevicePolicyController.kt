package com.eonmirth.panelguard.platform

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

interface DevicePolicyController {
    /** True only if this application is legitimately provisioned as Device Owner. */
    fun isDeviceOwner(): Boolean

    /** True if this application is a managed Profile Owner (work profile). */
    fun isProfileOwner(): Boolean

    /** True if Lock Task mode is permitted for this package by the active Device Owner. */
    fun isLockTaskPermitted(): Boolean

    /** Returns current configured Lock Task features if Device Owner, or 0. */
    fun getLockTaskFeatures(): Int

    /** Allow-lists this app and disables Lock Task system UI features for managed use. */
    fun configureManagedLockTask(): Boolean

    /** Removes this app from Lock Task and restores the system power-actions dialog. */
    fun restoreManagedLockTask(): Boolean
}

class AndroidDevicePolicyController(
    private val context: Context,
    private val adminComponent: ComponentName? =
        ComponentName(context, PanelGuardAdminReceiver::class.java)
) : DevicePolicyController {

    private val devicePolicyManager: DevicePolicyManager? by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    }

    override fun isDeviceOwner(): Boolean {
        return try {
            devicePolicyManager?.isDeviceOwnerApp(context.packageName) == true
        } catch (e: SecurityException) {
            false
        }
    }

    override fun isProfileOwner(): Boolean {
        return try {
            devicePolicyManager?.isProfileOwnerApp(context.packageName) == true
        } catch (e: SecurityException) {
            false
        }
    }

    override fun isLockTaskPermitted(): Boolean {
        return try {
            devicePolicyManager?.isLockTaskPermitted(context.packageName) == true
        } catch (e: SecurityException) {
            false
        }
    }

    override fun getLockTaskFeatures(): Int {
        if (!isDeviceOwner() || adminComponent == null) return 0
        return try {
            devicePolicyManager?.getLockTaskFeatures(adminComponent) ?: 0
        } catch (e: SecurityException) {
            0
        }
    }

    override fun configureManagedLockTask(): Boolean {
        if (!isDeviceOwner() || adminComponent == null) return false
        val manager = devicePolicyManager ?: return false
        return try {
            val packages = manager.getLockTaskPackages(adminComponent)
                .toMutableSet()
                .apply { add(context.packageName) }
            manager.setLockTaskPackages(adminComponent, packages.toTypedArray())
            manager.setLockTaskFeatures(
                adminComponent,
                DevicePolicyManager.LOCK_TASK_FEATURE_NONE
            )
            true
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    override fun restoreManagedLockTask(): Boolean {
        if (!isDeviceOwner() || adminComponent == null) return false
        val manager = devicePolicyManager ?: return false
        return try {
            val packages = manager.getLockTaskPackages(adminComponent)
                .filterNot { it == context.packageName }
            manager.setLockTaskPackages(adminComponent, packages.toTypedArray())
            manager.setLockTaskFeatures(
                adminComponent,
                DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
            )
            true
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

class ManagedLockTaskCoordinator(
    private val devicePolicyController: DevicePolicyController
) {
    fun configure(): Boolean {
        if (!devicePolicyController.isDeviceOwner()) return false
        return devicePolicyController.configureManagedLockTask()
    }

    fun restore(): Boolean {
        if (!devicePolicyController.isDeviceOwner()) return false
        return devicePolicyController.restoreManagedLockTask()
    }
}
