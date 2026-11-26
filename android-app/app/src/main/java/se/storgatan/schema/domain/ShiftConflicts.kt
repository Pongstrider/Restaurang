package se.storgatan.schema.domain

/**
 * Krock-regel: ett pass krockar om jag redan har ett pass samma dag.
 * Mina egna utlagda pass räknas – de är mina tills någon tar dem.
 * Servern gör samma kontroll och svarar 409, så appen är bara en förhandskoll.
 */
object ShiftConflicts {

    fun findConflict(candidate: Shift, myShifts: List<Shift>): Shift? =
        myShifts.firstOrNull { mine -> mine.id != candidate.id && mine.date == candidate.date }

    fun conflicts(candidate: Shift, myShifts: List<Shift>): Boolean =
        findConflict(candidate, myShifts) != null
}
