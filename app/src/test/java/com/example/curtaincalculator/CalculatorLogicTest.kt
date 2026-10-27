package com.example.curtaincalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorLogicTest {

    @Test
    fun rodDistance_keepsTenCentimeterDefault() {
        val result = calculateRodDistance(windowWidth = 200.0, rodWidth = 160.0)

        assertEquals(25.0, result, 0.0001)
    }

    @Test
    fun rodDistance_usesCustomDecimalDeduction() {
        val result = calculateRodDistance(
            windowWidth = 200.0,
            rodWidth = 160.0,
            deduction = 7.5
        )

        assertEquals(23.75, result, 0.0001)
    }

    @Test
    fun ringsCalculation_keepsOddDivisorAndAddsOneOnlyForDisplay() {
        val results = calculateRings(effectiveWidth = 400.0)
        val divisor25 = results.single { it.ringsCount == 25 }

        assertEquals(16.0, divisor25.spacing, 0.0001)
        assertEquals(26, divisor25.actualRingsCount)
        assertTrue(results.all { it.ringsCount % 2 == 1 })
    }

    @Test
    fun ringsCalculation_stillUsesInclusiveSpacingRange() {
        val results = calculateRings(effectiveWidth = 80.0)

        assertEquals(listOf(RingResult(ringsCount = 5, spacing = 16.0)), results)
        assertEquals(6, results.single().actualRingsCount)
    }

    @Test
    fun pressesPerMeter_dividesActualRingsCountByCurtainWidthInMeters() {
        val results = calculateRings(effectiveWidth = 530.0)
        val firstResult = results.first { it.ringsCount == 31 }

        assertEquals(32, firstResult.actualRingsCount)
        assertEquals(32.0 / 5.4, firstResult.pressesPerMeter(curtainWidthCm = 540.0), 0.0001)
    }

    @Test
    fun historyGroups_separatesEachSectionAndSupportsDeletingEntryOrDayGroup() {
        val windowDay1 = CalculationHistoryEntry(
            id = "1",
            dateKey = "2026-10-25",
            timeLabel = "08:00 م",
            calculatorType = CalculatorType.WINDOW,
            measurementSummary = "عرض الشباك: 200 | المسافة: 25 سم",
            timestampMillis = 1000L
        )
        val ringsDay2 = CalculationHistoryEntry(
            id = "2",
            dateKey = "2026-10-27",
            timeLabel = "09:00 م",
            calculatorType = CalculatorType.RINGS,
            measurementSummary = "عرض الستارة: 540 | عدد الحلقات: 32",
            timestampMillis = 2000L
        )
        val windowDay2 = CalculationHistoryEntry(
            id = "3",
            dateKey = "2026-10-27",
            timeLabel = "10:00 م",
            calculatorType = CalculatorType.WINDOW,
            measurementSummary = "عرض الشباك: 300 | المسافة: 30 سم",
            timestampMillis = 3000L
        )

        val allEntries = listOf(windowDay1, ringsDay2, windowDay2)

        val windowGroups = groupHistoryByDayForSection(allEntries, CalculatorType.WINDOW)
        val ringsGroups = groupHistoryByDayForSection(allEntries, CalculatorType.RINGS)

        assertEquals(2, windowGroups.size)
        assertEquals("2026-10-27", windowGroups[0].dateKey)
        assertTrue(windowGroups[0].outsideLabel.endsWith("27 / 10"))
        assertEquals(1, ringsGroups.size)
        assertEquals("2", ringsGroups[0].entries.single().id)

        // Delete an entire day group from WINDOW only; RINGS on the same date remains intact
        val afterWindowDayDelete =
            deleteHistoryDayGroup(allEntries, CalculatorType.WINDOW, "2026-10-27")
        assertEquals(
            1,
            groupHistoryByDayForSection(afterWindowDayDelete, CalculatorType.WINDOW).size
        )
        assertEquals(
            1,
            groupHistoryByDayForSection(afterWindowDayDelete, CalculatorType.RINGS).size
        )

        // Delete a single calculation
        val afterSingleDelete = deleteHistoryEntry(afterWindowDayDelete, "2")
        assertTrue(groupHistoryByDayForSection(afterSingleDelete, CalculatorType.RINGS).isEmpty())
    }

    @Test
    fun historySerialization_roundTripsMultilineEntries() {
        val original = listOf(
            CalculationHistoryEntry(
                id = "entry-1",
                dateKey = "2026-10-27",
                timeLabel = "09:15 م",
                calculatorType = CalculatorType.RINGS,
                measurementSummary = "عرض الستارة: 540.00 سم\nعدد الحلقات: 32",
                timestampMillis = 123456789L
            )
        )

        val serialized = serializeHistoryEntries(original)
        val restored = deserializeHistoryEntries(serialized)

        assertEquals(original, restored)
    }
}
