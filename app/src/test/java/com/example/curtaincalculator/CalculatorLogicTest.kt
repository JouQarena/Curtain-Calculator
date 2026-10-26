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
    fun historyFolders_groupsByDayAndSupportsDeletingSingleEntryOrEntireFolder() {
        val entryDay1A = CalculationHistoryEntry(
            id = "1",
            dateKey = "2026-10-25",
            timeLabel = "08:00 م",
            calculatorType = CalculatorType.WINDOW,
            inputsSummary = "عرض الشباك: 200",
            resultSummary = "المسافة: 25 سم",
            timestampMillis = 1000L
        )
        val entryDay2A = CalculationHistoryEntry(
            id = "2",
            dateKey = "2026-10-26",
            timeLabel = "09:00 م",
            calculatorType = CalculatorType.RINGS,
            inputsSummary = "عرض الستارة: 540",
            resultSummary = "عدد الحلقات: 32",
            timestampMillis = 2000L
        )
        val entryDay2B = CalculationHistoryEntry(
            id = "3",
            dateKey = "2026-10-26",
            timeLabel = "10:00 م",
            calculatorType = CalculatorType.WINDOW,
            inputsSummary = "عرض الشباك: 300",
            resultSummary = "المسافة: 30 سم",
            timestampMillis = 3000L
        )

        val allEntries = listOf(entryDay1A, entryDay2A, entryDay2B)
        val folders = groupHistoryByDay(allEntries)

        assertEquals(2, folders.size)
        assertEquals("2026-10-26", folders[0].dateKey)
        assertEquals(listOf("3", "2"), folders[0].entries.map { it.id })
        assertEquals("2026-10-25", folders[1].dateKey)

        // Delete a single calculation
        val afterSingleDelete = deleteHistoryEntry(allEntries, "2")
        val foldersAfterSingle = groupHistoryByDay(afterSingleDelete)
        assertEquals(1, foldersAfterSingle[0].entries.size)
        assertEquals("3", foldersAfterSingle[0].entries.single().id)

        // Delete an entire day's folder
        val afterFolderDelete = deleteHistoryFolder(afterSingleDelete, "2026-10-26")
        val foldersAfterFolder = groupHistoryByDay(afterFolderDelete)
        assertEquals(1, foldersAfterFolder.size)
        assertEquals("2026-10-25", foldersAfterFolder.single().dateKey)
    }

    @Test
    fun historySerialization_roundTripsMultilineEntries() {
        val original = listOf(
            CalculationHistoryEntry(
                id = "entry-1",
                dateKey = "2026-10-26",
                timeLabel = "09:15 م",
                calculatorType = CalculatorType.RINGS,
                inputsSummary = "عرض الستارة: 540.00 سم",
                resultSummary = "سطر 1\nسطر 2",
                timestampMillis = 123456789L
            )
        )

        val serialized = serializeHistoryEntries(original)
        val restored = deserializeHistoryEntries(serialized)

        assertEquals(original, restored)
    }
}
