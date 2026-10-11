package com.eonmirth.panelguard

import com.eonmirth.panelguard.domain.CapabilityStatus
import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.VerificationStatus
import com.eonmirth.panelguard.platform.DevicePolicyController
import com.eonmirth.panelguard.platform.LockStateReader
import com.eonmirth.panelguard.platform.OemCapabilityReader
import com.eonmirth.panelguard.platform.VerificationEvaluator
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 4 unit tests for [VerificationEvaluator].
 *
 * Each test names the evidence signal that determines the expected [VerificationStatus].
 * Tests use hand-rolled fakes — no mocking library required.
 */
class VerificationEvaluatorTest {

    // ─── Fakes ───────────────────────────────────────────────────────────────

    private fun fakePolicy(
        isDeviceOwner: Boolean = false,
        isProfileOwner: Boolean = false,
        isLockTaskPermitted: Boolean = false,
        lockTaskFeatures: Int = 0
    ): DevicePolicyController = object : DevicePolicyController {
        override fun isDeviceOwner() = isDeviceOwner
        override fun isProfileOwner() = isProfileOwner
        override fun isLockTaskPermitted() = isLockTaskPermitted
        override fun getLockTaskFeatures() = lockTaskFeatures
        override fun configureManagedLockTask() = false
        override fun restoreManagedLockTask() = false
    }

    private fun fakeLock(isSecure: Boolean = true, isLocked: Boolean = false): LockStateReader =
        object : LockStateReader {
            override fun isDeviceSecure() = isSecure
            override fun isKeyguardLocked() = isLocked
        }

    private fun makeEvaluator(
        policy: DevicePolicyController,
        lock: LockStateReader
    ): VerificationEvaluator {
        val capabilityReader = OemCapabilityReader(policy, lock)
        return VerificationEvaluator(policy, lock, capabilityReader)
    }

    // ─── Device Owner + Lock Task active + secure → MANAGED_ONLY ─────────────

    @Test
    fun `device owner with lock task and secure lock returns MANAGED_ONLY`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(isDeviceOwner = true, isLockTaskPermitted = true),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.GOOGLE, 33)
        assertEquals(
            "Evidence: isDeviceOwnerApp=true, isLockTaskPermitted=true, isDeviceSecure=true → MANAGED_ONLY",
            VerificationStatus.MANAGED_ONLY,
            result.status
        )
        assertTrue("isProtected should be true for MANAGED_ONLY", result.isProtected)
        assertTrue("Reason must mention isDeviceOwnerApp", result.reason.contains("isDeviceOwnerApp=true"))
        assertTrue("Reason must mention isLockTaskPermitted", result.reason.contains("isLockTaskPermitted=true"))
        assertTrue("Reason must mention isDeviceSecure", result.reason.contains("isDeviceSecure=true"))
    }

    // ─── Device Owner + Lock Task active + NO secure lock → PARTIAL ──────────

    @Test
    fun `device owner with lock task but insecure lock returns PARTIAL`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(isDeviceOwner = true, isLockTaskPermitted = true),
            lock = fakeLock(isSecure = false)
        )
        val result = evaluator.evaluate(OemFamily.GOOGLE, 33)
        assertEquals(VerificationStatus.PARTIAL, result.status)
        assertFalse("isProtected should be false for PARTIAL", result.isProtected)
        assertTrue(result.reason.contains("isDeviceSecure=false"))
    }

    // ─── Device Owner enrolled but Lock Task NOT configured → SETUP_REQUIRED ─

    @Test
    fun `device owner without lock task returns SETUP_REQUIRED`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(isDeviceOwner = true, isLockTaskPermitted = false),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.GOOGLE, 33)
        assertEquals(VerificationStatus.SETUP_REQUIRED, result.status)
        assertTrue(result.reason.contains("isLockTaskPermitted=false"))
        assertNotNull("Should have actionLabel", result.actionLabel)
    }

    // ─── No secure credential → SETUP_REQUIRED ───────────────────────────────

    @Test
    fun `insecure device without device owner returns SETUP_REQUIRED`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(isDeviceOwner = false),
            lock = fakeLock(isSecure = false)
        )
        val result = evaluator.evaluate(OemFamily.SAMSUNG, 33)
        assertEquals(VerificationStatus.SETUP_REQUIRED, result.status)
        assertTrue(result.reason.contains("isDeviceSecure=false"))
    }

    // ─── Samsung + OEM setting available + NOT confirmed → SETUP_REQUIRED ────

    @Test
    fun `samsung with oem setting available but not confirmed returns SETUP_REQUIRED`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.SAMSUNG, 33, userConfirmedOemSetting = false)
        assertEquals(VerificationStatus.SETUP_REQUIRED, result.status)
        assertTrue(result.reason.contains("userConfirmedOemSetting=false"))
    }

    // ─── Samsung + OEM setting confirmed → PARTIAL (not ACTIVE, never verifiable) ─

    @Test
    fun `samsung with oem setting confirmed returns PARTIAL not ACTIVE`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.SAMSUNG, 33, userConfirmedOemSetting = true)
        assertEquals(
            "OEM toggle is self-attested only — must be PARTIAL, never ACTIVE",
            VerificationStatus.PARTIAL,
            result.status
        )
        assertFalse("isProtected must be false for self-attested PARTIAL", result.isProtected)
        assertTrue(result.reason.contains("userConfirmedOemSetting=true"))
        assertTrue(result.reason.contains("self-attestation"))
    }

    // ─── Xiaomi + OEM setting not confirmed → SETUP_REQUIRED ─────────────────

    @Test
    fun `xiaomi with oem setting not confirmed returns SETUP_REQUIRED`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.XIAOMI, 33, userConfirmedOemSetting = false)
        assertEquals(VerificationStatus.SETUP_REQUIRED, result.status)
    }

    // ─── Google Pixel (AOSP) → UNSUPPORTED ────────────────────────────────────

    @Test
    fun `google pixel returns UNSUPPORTED consumer shade toggle`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.GOOGLE, 33)
        assertEquals(VerificationStatus.UNSUPPORTED, result.status)
        assertFalse("isProtected must be false for UNSUPPORTED", result.isProtected)
        assertTrue(result.reason.contains("hasKnownLockScreenShadeSetting=false"))
    }

    // ─── Motorola → UNSUPPORTED ────────────────────────────────────────────────

    @Test
    fun `motorola returns UNSUPPORTED`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.MOTOROLA, 33)
        assertEquals(VerificationStatus.UNSUPPORTED, result.status)
    }

    // ─── Unknown OEM → UNKNOWN ─────────────────────────────────────────────────

    @Test
    fun `unrecognised OEM returns UNKNOWN`() {
        val evaluator = makeEvaluator(
            policy = fakePolicy(),
            lock = fakeLock(isSecure = true)
        )
        val result = evaluator.evaluate(OemFamily.GENERIC, 33)
        assertEquals(VerificationStatus.UNKNOWN, result.status)
        assertFalse("isProtected must be false for UNKNOWN", result.isProtected)
    }

    // ─── Error path: exception thrown by policy → ERROR ──────────────────────

    @Test
    fun `exception thrown by device policy controller returns ERROR status`() {
        val crashingPolicy = object : DevicePolicyController {
            override fun isDeviceOwner() = throw RuntimeException("Simulated policy failure")
            override fun isProfileOwner() = false
            override fun isLockTaskPermitted() = false
            override fun getLockTaskFeatures() = 0
            override fun configureManagedLockTask() = false
            override fun restoreManagedLockTask() = false
        }
        val capabilityReader = OemCapabilityReader(crashingPolicy, fakeLock())
        val evaluator = VerificationEvaluator(crashingPolicy, fakeLock(), capabilityReader)
        val result = evaluator.evaluate(OemFamily.GOOGLE, 33)
        assertEquals(VerificationStatus.ERROR, result.status)
        assertTrue(result.reason.contains("RuntimeException"))
    }

    // ─── Capabilities list is non-empty for all branches ─────────────────────

    @Test
    fun `all evaluation paths return non-empty capabilities`() {
        val cases = listOf(
            Triple(fakePolicy(isDeviceOwner = true, isLockTaskPermitted = true), fakeLock(true), OemFamily.GOOGLE),
            Triple(fakePolicy(), fakeLock(false), OemFamily.SAMSUNG),
            Triple(fakePolicy(), fakeLock(true), OemFamily.SAMSUNG),
            Triple(fakePolicy(), fakeLock(true), OemFamily.GOOGLE),
            Triple(fakePolicy(), fakeLock(true), OemFamily.GENERIC)
        )
        cases.forEachIndexed { idx, (policy, lock, oem) ->
            val reader = OemCapabilityReader(policy, lock)
            val evaluator = VerificationEvaluator(policy, lock, reader)
            val result = evaluator.evaluate(oem, 33)
            assertTrue(
                "Case $idx ($oem): capabilities list must not be empty",
                result.capabilities.isNotEmpty()
            )
        }
    }

    // ─── Reason string is never blank ─────────────────────────────────────────

    @Test
    fun `all evaluation paths produce non-blank reason strings`() {
        val cases = listOf(
            Triple(fakePolicy(isDeviceOwner = true, isLockTaskPermitted = true), fakeLock(true), OemFamily.GOOGLE),
            Triple(fakePolicy(isDeviceOwner = true, isLockTaskPermitted = false), fakeLock(true), OemFamily.GOOGLE),
            Triple(fakePolicy(), fakeLock(false), OemFamily.GOOGLE),
            Triple(fakePolicy(), fakeLock(true), OemFamily.SAMSUNG),
            Triple(fakePolicy(), fakeLock(true), OemFamily.GOOGLE),
            Triple(fakePolicy(), fakeLock(true), OemFamily.GENERIC)
        )
        cases.forEachIndexed { idx, (policy, lock, oem) ->
            val reader = OemCapabilityReader(policy, lock)
            val evaluator = VerificationEvaluator(policy, lock, reader)
            val result = evaluator.evaluate(oem, 33)
            assertTrue(
                "Case $idx ($oem): reason must not be blank, got: '${result.reason}'",
                result.reason.isNotBlank()
            )
        }
    }
}
