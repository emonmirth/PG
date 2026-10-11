package com.eonmirth.panelguard

import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.ProtectionLevel
import com.eonmirth.panelguard.domain.ProtectionMode
import com.eonmirth.panelguard.platform.OemCapabilityReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumerSetupMappingTest {

    private val fakePolicy = FakeDevicePolicyController()
    private val fakeLock = FakeLockStateReader(isDeviceSecureValue = true)
    private val reader = OemCapabilityReader(fakePolicy, fakeLock)

    // --- 1. Every OEM Mapping Verification ---

    @Test
    fun samsungMapping_containsLockNetworkAndSecurityGuidance() {
        val steps = OemFamily.SAMSUNG.getGuidanceSteps()
        assertTrue(steps.any { it.contains("Lock network and security") })
        assertTrue(OemFamily.SAMSUNG.hasKnownLockScreenShadeSetting)
    }

    @Test
    fun xiaomiMapping_containsControlCenterGuidance() {
        val steps = OemFamily.XIAOMI.getGuidanceSteps()
        assertTrue(steps.any { it.contains("Control center") })
        assertTrue(OemFamily.XIAOMI.hasKnownLockScreenShadeSetting)
    }

    @Test
    fun oppoAndOnePlusMapping_containsSwipeDownGuidance() {
        val oppoSteps = OemFamily.OPPO.getGuidanceSteps()
        assertTrue(oppoSteps.any { it.contains("Swipe down on Lock screen") })
        assertTrue(OemFamily.OPPO.hasKnownLockScreenShadeSetting)

        val onePlusSteps = OemFamily.ONEPLUS.getGuidanceSteps()
        assertTrue(onePlusSteps.any { it.contains("Swipe down on Lock screen") })
        assertTrue(OemFamily.ONEPLUS.hasKnownLockScreenShadeSetting)
    }

    @Test
    fun vivoMapping_containsLockScreenControlCenterGuidance() {
        val steps = OemFamily.VIVO.getGuidanceSteps()
        assertTrue(steps.any { it.contains("Control Center on the lock screen") })
        assertTrue(OemFamily.VIVO.hasKnownLockScreenShadeSetting)
    }

    @Test
    fun googlePixelMapping_doesNotClaimNativeSetting() {
        val steps = OemFamily.GOOGLE.getGuidanceSteps()
        assertFalse(OemFamily.GOOGLE.hasKnownLockScreenShadeSetting)
        assertTrue(steps.any { it.contains("Lockdown") })
    }

    @Test
    fun motorolaMapping_doesNotClaimNativeSetting() {
        val steps = OemFamily.MOTOROLA.getGuidanceSteps()
        assertFalse(OemFamily.MOTOROLA.hasKnownLockScreenShadeSetting)
        assertTrue(steps.any { it.contains("Lockdown") })
    }

    // --- 2. User Confirmation Transitions ---

    @Test
    fun userConfirmation_transitionsStateFromPartialToActive() {
        // Initial state before confirmation
        val unconfirmed = reader.evaluateProtectionState(
            oemFamily = OemFamily.SAMSUNG,
            sdkInt = 34,
            userConfirmedOemSetting = false
        )
        assertEquals(ProtectionLevel.PARTIAL, unconfirmed.level)
        assertEquals(ProtectionMode.NONE, unconfirmed.mode)
        assertFalse(unconfirmed.isProtected)

        // After user confirmation
        val confirmed = reader.evaluateProtectionState(
            oemFamily = OemFamily.SAMSUNG,
            sdkInt = 34,
            userConfirmedOemSetting = true
        )
        assertEquals(ProtectionLevel.ACTIVE, confirmed.level)
        assertEquals(ProtectionMode.OEM_SETTING, confirmed.mode)
        assertTrue(confirmed.isProtected)

        // If user unchecks confirmation
        val revoked = reader.evaluateProtectionState(
            oemFamily = OemFamily.SAMSUNG,
            sdkInt = 34,
            userConfirmedOemSetting = false
        )
        assertEquals(ProtectionLevel.PARTIAL, revoked.level)
        assertFalse(revoked.isProtected)
    }

    @Test
    fun insecureDevice_cannotBeActiveEvenIfUserConfirms() {
        fakeLock.isDeviceSecureValue = false

        val state = reader.evaluateProtectionState(
            oemFamily = OemFamily.SAMSUNG,
            sdkInt = 34,
            userConfirmedOemSetting = true
        )
        // Must refuse ACTIVE because physical lock security is missing
        assertEquals(ProtectionLevel.NOT_ACTIVE, state.level)
        assertFalse(state.isProtected)
    }
}
