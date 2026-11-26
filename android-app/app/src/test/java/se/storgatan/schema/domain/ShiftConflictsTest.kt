package se.storgatan.schema.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ShiftConflictsTest {

    private fun shift(id: String, day: Int, type: ShiftType, status: ShiftStatus = ShiftStatus.ASSIGNED): Shift {
        val (start, end) = when (type) {
            ShiftType.LUNCH -> LocalTime.of(10, 0) to LocalTime.of(15, 0)
            ShiftType.MIDDAG -> LocalTime.of(16, 0) to LocalTime.of(23, 0)
        }
        return Shift(id, LocalDate.of(2026, 12, day), type, start, end, "e-erik", status)
    }

    private val mine = listOf(
        shift("m1", 1, ShiftType.MIDDAG),
        shift("m2", 4, ShiftType.MIDDAG),
    )

    @Test
    fun `pass samma dag krockar även om tiderna inte överlappar`() {
        val lunchSameDay = shift("x", 4, ShiftType.LUNCH)
        assertEquals("m2", ShiftConflicts.findConflict(lunchSameDay, mine)?.id)
    }

    @Test
    fun `pass en annan dag krockar inte`() {
        assertNull(ShiftConflicts.findConflict(shift("x", 3, ShiftType.MIDDAG), mine))
    }

    @Test
    fun `samma pass jämförs inte med sig självt`() {
        assertFalse(ShiftConflicts.conflicts(mine[0], mine))
    }

    @Test
    fun `mitt eget utlagda pass räknas fortfarande som mitt`() {
        val released = listOf(shift("m3", 5, ShiftType.MIDDAG, ShiftStatus.RELEASED))
        assertTrue(ShiftConflicts.conflicts(shift("x", 5, ShiftType.LUNCH), released))
    }

    @Test
    fun `inga egna pass ger ingen krock`() {
        assertFalse(ShiftConflicts.conflicts(shift("x", 5, ShiftType.LUNCH), emptyList()))
    }
}
