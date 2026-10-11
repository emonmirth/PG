package com.eonmirth.panelguard

import com.eonmirth.panelguard.domain.CapabilityStatus
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.ProtectionLevel
import com.eonmirth.panelguard.domain.ProtectionMode
import com.eonmirth.panelguard.platform.DevicePolicyController
import com.eonmirth.panelguard.platform.ManagedLockTaskCoordinator
import com.eonmirth.panelguard.platform.LockStateReader
import com.eonmirth.panelguard.platform.OemCapabilityReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeDevicePolicyController(
    var isDeviceOwnerValue: Boolean = false,
    var isProfileOwnerValue: Boolean = false,
    var isLockTaskPermittedValue: Boolean = false,
    var lockTaskFeaturesValue: Int = 0
) : DevicePolicyController {
    var configureManagedLockTaskCalls = 0
    var restoreManagedLockTaskCalls = 0

    override fun isDeviceOwner(): Boolean = isDeviceOwnerValue
    override fun isProfileOwner(): Boolean = isProfileOwnerValue
    override fun isLockTaskPermitted(): Boolean = isLockTaskPermittedValue
    override fun getLockTaskFeatures(): Int = lockTaskFeaturesValue
    override fun configureManagedLockTask(): Boolean {
        configureManagedLockTaskCalls++
        return true
    }
    override fun restoreManagedLockTask(): Boolean {
        restoreManagedLockTaskCalls++
        return true
    }
}

class FakeLockStateReader(
    var isDeviceSecureValue: Boolean = true,
    var isKeyguardLockedValue: Boolean = false
) : LockStateReader {
    override fun isDeviceSecure(): Boolean = isDeviceSecureValue
    override fun isKeyguardLocked(): Boolean = isKeyguardLockedValue
}

class CapabilityEngineTest {

    private val fakePolicy = FakeDevicePolicyController()
    private val fakeLock = FakeLockStateReader()
    private val reader = OemCapabilityReader(fakePolicy, fakeLock)

    // --- 1. Manufacturer & Brand Normalization Tests ---

    @Test
    fun oemDetection_normalizesAllMajorBrands() {
        assertEquals(OemFamily.SAMSUNG, OemFamily.detect("samsung", "galaxy"))
        assertEquals(OemFamily.XIAOMI, OemFamily.detect("Xiaomi", "POCO"))
        assertEquals(OemFamily.XIAOMI, OemFamily.detect("Redmi", "Note"))
        assertEquals(OemFamily.OPPO, OemFamily.detect("OPPO", "Find"))
        assertEquals(OemFamily.OPPO, OemFamily.detect("realme", "GT"))
        assertEquals(OemFamily.ONEPLUS, OemFamily.detect("OnePlus", "12"))
        assertEquals(OemFamily.VIVO, OemFamily.detect("vivo", "X100"))
        assertEquals(OemFamily.VIVO, OemFamily.detect("iQOO", "12"))
        assertEquals(OemFamily.GOOGLE, OemFamily.detect("Google", "Pixel 9"))
        assertEquals(OemFamily.MOTOROLA, OemFamily.detect("motorola", "edge"))
        assertEquals(OemFamily.MOTOROLA, OemFamily.detect("Lenovo", "Tab"))
        assertEquals(OemFamily.GENERIC, OemFamily.detect("UnknownMaker", "CustomBrand"))
        assertEquals(OemFamily.GENERIC, OemFamily.detect(null, null))
        assertEquals(OemFamily.GENERIC, OemFamily.detect("", ""))
    }

    // --- 2. Device Owner & Lock Task State Handling ---

    @Test
    fun ordinaryUser_isNeverTreatedAsDeviceOwner() {
        fakePolicy.isDeviceOwnerValue = false
        val capabilities = reader.evaluateCapabilities(OemFamily.SAMSUNG, sdkInt = 34)

        val lockTaskCap = capabilities.first { it.id == "cap_managed_lock_task" }
        assertEquals(CapabilityStatus.MANAGED_ONLY, lockTaskCap.status)
        assertTrue(lockTaskCap.confidenceReason.contains("cannot be granted without enterprise provisioning"))

        val state = reader.evaluateProtectionState(OemFamily.SAMSUNG, sdkInt = 34)
        assertFalse(state.level == ProtectionLevel.MANAGED_ENFORCED)
    }

    @Test
    fun ordinaryUser_cannotConfigureOrRestoreManagedLockTask() {
        fakePolicy.isDeviceOwnerValue = false
        fakePolicy.isProfileOwnerValue = true
        val coordinator = ManagedLockTaskCoordinator(fakePolicy)

        assertFalse(coordinator.configure())
        assertFalse(coordinator.restore())
        assertEquals(0, fakePolicy.configureManagedLockTaskCalls)
        assertEquals(0, fakePolicy.restoreManagedLockTaskCalls)
    }

    @Test
    fun deviceOwnerWithLockTask_isEvaluatedAsManagedEnforced() {
        fakePolicy.isDeviceOwnerValue = true
        fakePolicy.isLockTaskPermittedValue = true

        val capabilities = reader.evaluateCapabilities(OemFamily.GOOGLE, sdkInt = 34)
        val lockTaskCap = capabilities.first { it.id == "cap_managed_lock_task" }
        assertEquals(CapabilityStatus.SUPPORTED, lockTaskCap.status)

        val state = reader.evaluateProtectionState(OemFamily.GOOGLE, sdkInt = 34)
        assertEquals(ProtectionLevel.MANAGED_ENFORCED, state.level)
        assertEquals(ProtectionMode.MANAGED_LOCK_TASK, state.mode)
        assertTrue(state.isProtected)
    }

    @Test
    fun deviceOwnerWithoutLockTaskPermitted_isPartialState() {
        fakePolicy.isDeviceOwnerValue = true
        fakePolicy.isLockTaskPermittedValue = false

        val state = reader.evaluateProtectionState(OemFamily.GOOGLE, sdkInt = 34)
        assertEquals(ProtectionLevel.PARTIAL, state.level)
        assertEquals(ProtectionMode.NONE, state.mode)
    }

    // --- 3. Screen Lock Security Check ---

    @Test
    fun insecureDevice_isEvaluatedAsNotActive() {
        fakePolicy.isDeviceOwnerValue = false
        fakeLock.isDeviceSecureValue = false

        val state = reader.evaluateProtectionState(OemFamily.SAMSUNG, sdkInt = 34)
        assertEquals(ProtectionLevel.NOT_ACTIVE, state.level)
        assertEquals("Lock screen credential not configured", state.summary)
    }

    // --- 4. OEM Guidance vs Stock Android vs Unknown Truthfulness ---

    @Test
    fun samsungDevice_evaluatesToOemGuidance_andActiveWhenConfirmed() {
        fakePolicy.isDeviceOwnerValue = false
        fakeLock.isDeviceSecureValue = true

        // Before user confirmation
        val unconfirmed = reader.evaluateProtectionState(
            OemFamily.SAMSUNG,
            sdkInt = 34,
            userConfirmedOemSetting = false
        )
        assertEquals(ProtectionLevel.PARTIAL, unconfirmed.level)
        assertEquals(ProtectionMode.NONE, unconfirmed.mode)

        // After user confirmation
        val confirmed = reader.evaluateProtectionState(
            OemFamily.SAMSUNG,
            sdkInt = 34,
            userConfirmedOemSetting = true
        )
        assertEquals(ProtectionLevel.ACTIVE, confirmed.level)
        assertEquals(ProtectionMode.OEM_SETTING, confirmed.mode)
        assertTrue(confirmed.isProtected)
    }

    @Test
    fun googlePixel_reportsPartialAndDoesNotFakeSupport() {
        fakePolicy.isDeviceOwnerValue = false
        fakeLock.isDeviceSecureValue = true

        val state = reader.evaluateProtectionState(OemFamily.GOOGLE, sdkInt = 34)
        assertEquals(ProtectionLevel.PARTIAL, state.level)
        assertEquals(ProtectionMode.NONE, state.mode)
        assertTrue(state.verifiedReason.orEmpty().contains("Stock AOSP does not expose consumer shade restriction"))
    }

    @Test
    fun unknownDevice_remainsUnknownAndNeverAssumedSupported() {
        fakePolicy.isDeviceOwnerValue = false
        fakeLock.isDeviceSecureValue = true

        val state = reader.evaluateProtectionState(OemFamily.GENERIC, sdkInt = 34)
        assertEquals(ProtectionLevel.UNKNOWN, state.level)
        assertEquals(ProtectionMode.NONE, state.mode)

        val caps = reader.evaluateCapabilities(OemFamily.GENERIC, sdkInt = 34)
        val shadeCap = caps.first { it.id == "cap_oem_shade_toggle" }
        assertEquals(CapabilityStatus.UNKNOWN, shadeCap.status)
    }

    @Test
    fun allOemFamilies_haveGuidanceSteps() {
        for (oem in OemFamily.values()) {
            val steps = oem.getGuidanceSteps()
            assertTrue("OEM ${oem.name} must have guidance steps", steps.isNotEmpty())
        }
    }
}
