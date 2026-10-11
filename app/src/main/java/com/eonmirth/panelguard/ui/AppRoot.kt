package com.eonmirth.panelguard.ui

import androidx.compose.runtime.*
import com.eonmirth.panelguard.domain.Capability
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.ProtectionState
import com.eonmirth.panelguard.domain.VerificationResult
import com.eonmirth.panelguard.domain.VerificationStatus

enum class Screen {
    HOME,
    COMPATIBILITY,
    SETUP,
    VERIFICATION
}

@Composable
fun AppRoot(
    oemFamily: OemFamily,
    capabilities: List<Capability>,
    protectionState: ProtectionState,
    managedDeviceOwner: Boolean,
    lockTaskPermitted: Boolean,
    latestVerificationResult: VerificationResult,
    isVerifying: Boolean,
    onOpenSettings: () -> Unit,
    onUserConfirmedSetting: (Boolean) -> Unit,
    onStartManagedLockTask: () -> Unit,
    onExitManagedLockTask: () -> Unit,
    onRequestVerification: () -> Unit,
    onVerificationAction: (VerificationResult) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    when (currentScreen) {
        Screen.HOME -> HomeScreen(
            oemFamily = oemFamily,
            protectionState = protectionState,
            managedDeviceOwner = managedDeviceOwner,
            lockTaskPermitted = lockTaskPermitted,
            onViewCapabilitiesClicked = { currentScreen = Screen.COMPATIBILITY },
            onSetupGuideClicked = { currentScreen = Screen.SETUP },
            onVerifyProtectionClicked = {
                onRequestVerification()
                currentScreen = Screen.VERIFICATION
            },
            onStartManagedLockTask = onStartManagedLockTask,
            onExitManagedLockTask = onExitManagedLockTask
        )

        Screen.COMPATIBILITY -> CompatibilityScreen(
            oemFamily = oemFamily,
            capabilities = capabilities,
            onBackClicked = { currentScreen = Screen.HOME },
            onProceedToSetupClicked = { currentScreen = Screen.SETUP }
        )

        Screen.SETUP -> SetupScreen(
            oemFamily = oemFamily,
            initialConfirmed = protectionState.isProtected,
            onBackClicked = { currentScreen = Screen.HOME },
            onOpenSettingsClicked = onOpenSettings,
            onConfirmationChanged = onUserConfirmedSetting,
            onFinishSetupClicked = { currentScreen = Screen.HOME }
        )

        Screen.VERIFICATION -> VerificationScreen(
            result = latestVerificationResult,
            isRefreshing = isVerifying,
            onRefreshClicked = onRequestVerification,
            onActionClicked = {
                // Route the CTA to the correct screen based on the result status
                when (latestVerificationResult.status) {
                    VerificationStatus.SETUP_REQUIRED,
                    VerificationStatus.UNSUPPORTED -> currentScreen = Screen.SETUP
                    VerificationStatus.UNKNOWN -> currentScreen = Screen.COMPATIBILITY
                    else -> onVerificationAction(latestVerificationResult)
                }
            },
            onBackClicked = { currentScreen = Screen.HOME }
        )
    }
}
