package com.eonmirth.panelguard

import com.eonmirth.panelguard.domain.OemFamily
import com.eonmirth.panelguard.domain.ProtectionLevel
import com.eonmirth.panelguard.domain.ProtectionMode
import com.eonmirth.panelguard.domain.ProtectionState
import org.junit.Assert.assertEquals
import org.junit.Test

class BaselineUnitTest {

    @Test
    fun oemDetection_identifiesSamsungCorrectly() {
        val oem = OemFamily.detect(manufacturer = "samsung", brand = "samsung")
        assertEquals(OemFamily.SAMSUNG, oem)
    }

    @Test
    fun oemDetection_identifiesXiaomiPocoCorrectly() {
        val oem = OemFamily.detect(manufacturer = "Xiaomi", brand = "POCO")
        assertEquals(OemFamily.XIAOMI, oem)
    }

    @Test
    fun oemDetection_identifiesGooglePixelCorrectly() {
        val oem = OemFamily.detect(manufacturer = "Google", brand = "google")
        assertEquals(OemFamily.GOOGLE, oem)
    }

    @Test
    fun oemDetection_defaultsToGenericForUnknown() {
        val oem = OemFamily.detect(manufacturer = "UnknownMaker", brand = "CustomBrand")
        assertEquals(OemFamily.GENERIC, oem)
    }

    @Test
    fun initialProtectionState_isNotActive() {
        val state = ProtectionState.initial()
        assertEquals(ProtectionLevel.NOT_ACTIVE, state.level)
        assertEquals(ProtectionMode.NONE, state.mode)
    }
}
