package com.eonmirth.panelguard

import android.content.ActivityNotFoundException
import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.VerificationResult
import com.eonmirth.panelguard.domain.VerificationStatus
import com.eonmirth.panelguard.platform.AndroidDevicePolicyController
import com.eonmirth.panelguard.platform.AndroidLockStateReader
import com.eonmirth.panelguard.platform.AndroidSettingsNavigator
import com.eonmirth.panelguard.platform.ManagedLockTaskCoordinator
import com.eonmirth.panelguard.platform.OemCapabilityReader
import com.eonmirth.panelguard.platform.VerificationEvaluator
import com.eonmirth.panelguard.ui.AppRoot
import com.eonmirth.panelguard.ui.theme.PanelGuardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val oemFamily = OemFamily.detect(
            manufacturer = Build.MANUFACTURER,
            brand = Build.BRAND
        )

        val devicePolicyController = AndroidDevicePolicyController(this)
        val managedLockTaskCoordinator = ManagedLockTaskCoordinator(devicePolicyController)
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val lockStateReader = AndroidLockStateReader(this)
        val capabilityReader = OemCapabilityReader(devicePolicyController, lockStateReader)
        val settingsNavigator = AndroidSettingsNavigator(this)

        // Phase 4: Verification evaluator
        val verificationEvaluator = VerificationEvaluator(
            devicePolicyController = devicePolicyController,
            lockStateReader = lockStateReader,
            oemCapabilityReader = capabilityReader
        )

        val capabilities = capabilityReader.evaluateCapabilities(
            oemFamily = oemFamily,
            sdkInt = Build.VERSION.SDK_INT
        )

        setContent {
            var userConfirmed by remember { mutableStateOf(false) }
            val managedDeviceOwner = remember { devicePolicyController.isDeviceOwner() }
            var lockTaskPermitted by remember {
                mutableStateOf(devicePolicyController.isLockTaskPermitted())
            }

            val protectionState = remember(userConfirmed) {
                capabilityReader.evaluateProtectionState(
                    oemFamily = oemFamily,
                    sdkInt = Build.VERSION.SDK_INT,
                    userConfirmedOemSetting = userConfirmed
                )
            }

            // Phase 4: Verification state
            var isVerifying by remember { mutableStateOf(false) }
            var latestVerificationResult by remember {
                mutableStateOf(
                    VerificationResult(
                        status = VerificationStatus.UNKNOWN,
                        title = "Not Yet Verified",
                        summary = "Tap 'Verify Protection' to evaluate the current device state.",
                        reason = "Initial state — evaluation not yet requested."
                    )
                )
            }

            fun runVerification() {
                isVerifying = true
                latestVerificationResult = verificationEvaluator.evaluate(
                    oemFamily = oemFamily,
                    sdkInt = Build.VERSION.SDK_INT,
                    userConfirmedOemSetting = userConfirmed
                )
                isVerifying = false
            }

            PanelGuardTheme {
                AppRoot(
                    oemFamily = oemFamily,
                    capabilities = capabilities,
                    protectionState = protectionState,
                    managedDeviceOwner = managedDeviceOwner,
                    lockTaskPermitted = lockTaskPermitted,
                    latestVerificationResult = latestVerificationResult,
                    isVerifying = isVerifying,
                    onOpenSettings = {
                        val intent = settingsNavigator.getOemSettingIntent(oemFamily)
                            ?: settingsNavigator.getSecuritySettingsIntent()
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                this,
                                "Could not open settings automatically. Please use the manual steps.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onUserConfirmedSetting = { confirmed ->
                        userConfirmed = confirmed
                    },
                    onRequestVerification = {
                        runVerification()
                    },
                    onVerificationAction = { result ->
                        // MANAGED_ONLY action: lock task was already active or just needs info
                        // PARTIAL action for Device Owner — guide to configure
                        if (result.status == VerificationStatus.MANAGED_ONLY) {
                            Toast.makeText(
                                this,
                                "Enterprise protection is active. No further action needed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onStartManagedLockTask = {
                        when {
                            !managedDeviceOwner -> Toast.makeText(
                                this,
                                "Managed mode requires this app to be provisioned as Device Owner.",
                                Toast.LENGTH_LONG
                            ).show()
                            keyguardManager.isKeyguardLocked -> Toast.makeText(
                                this,
                                "Unlock the device before starting managed Lock Task.",
                                Toast.LENGTH_LONG
                            ).show()
                            !managedLockTaskCoordinator.configure() -> Toast.makeText(
                                this,
                                "Could not configure managed Lock Task. Ask the device administrator to check provisioning.",
                                Toast.LENGTH_LONG
                            ).show()
                            else -> try {
                                startLockTask()
                                lockTaskPermitted = devicePolicyController.isLockTaskPermitted()
                            } catch (e: SecurityException) {
                                managedLockTaskCoordinator.restore()
                                lockTaskPermitted = devicePolicyController.isLockTaskPermitted()
                                Toast.makeText(
                                    this,
                                    "Android refused to start managed Lock Task. Managed settings were restored.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: IllegalStateException) {
                                managedLockTaskCoordinator.restore()
                                lockTaskPermitted = devicePolicyController.isLockTaskPermitted()
                                Toast.makeText(
                                    this,
                                    "Managed Lock Task could not start. Managed settings were restored.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onExitManagedLockTask = {
                        runCatching { stopLockTask() }
                        val restored = managedLockTaskCoordinator.restore()
                        lockTaskPermitted = devicePolicyController.isLockTaskPermitted()
                        Toast.makeText(
                            this,
                            if (restored) {
                                "Managed mode exited; this app was removed from the Lock Task allowlist."
                            } else {
                                "Could not restore managed settings. Ask the device administrator for help."
                            },
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }
}
