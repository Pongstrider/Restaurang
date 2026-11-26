package se.storgatan.schema.data.fake

import se.storgatan.schema.domain.Employee
import se.storgatan.schema.domain.Notification
import se.storgatan.schema.domain.NotificationType
import se.storgatan.schema.domain.ReleaseTarget
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftStatus
import se.storgatan.schema.domain.ShiftType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import se.storgatan.schema.domain.ShiftFormat

/**
 * Testdata: vecka 49, 30 nov–6 dec 2026. Inloggad användare är Erik.
 * Logga in med erik@storgatan.se / storgatan (alla anställda fungerar: <förnamn>@storgatan.se).
 */
object FakeData {
    const val PASSWORD = "storgatan"
    val ZONE: ZoneId = ShiftFormat.ZONE

    val erik = Employee("e-erik", "Erik")
    val sara = Employee("e-sara", "Sara")
    val lina = Employee("e-lina", "Lina")
    val omar = Employee("e-omar", "Omar")
    val maja = Employee("e-maja", "Maja")
    val johan = Employee("e-johan", "Johan")
    val elin = Employee("e-elin", "Elin")

    /** Anders är chef och byter inga pass, men får notiser (via backend). */
    const val MANAGER_ID = "e-anders"

    val employees: List<Employee> = listOf(erik, sara, lina, omar, maja, johan, elin)

    fun emailFor(employee: Employee): String = "${employee.name.lowercase()}@storgatan.se"

    private fun d(day: Int, month: Int = 12): LocalDate = LocalDate.of(2026, month, day)
    private val lunchStart = LocalTime.of(10, 0)
    private val lunchEnd = LocalTime.of(15, 0)
    private val dinnerStart = LocalTime.of(16, 0)
    private val dinnerEnd = LocalTime.of(23, 0)

    fun at(day: Int, month: Int, hour: Int, minute: Int): Instant =
        LocalDateTime.of(2026, month, day, hour, minute).atZone(ZONE).toInstant()

    private fun lunch(id: String, date: LocalDate, who: Employee) =
        Shift(id, date, ShiftType.LUNCH, lunchStart, lunchEnd, who.id, ShiftStatus.ASSIGNED)

    private fun dinner(id: String, date: LocalDate, who: Employee) =
        Shift(id, date, ShiftType.MIDDAG, dinnerStart, dinnerEnd, who.id, ShiftStatus.ASSIGNED)

    fun shifts(): List<Shift> = listOf(
        // Eriks pass
        dinner("p-101", d(1), erik),
        lunch("p-102", d(2), erik),
        dinner("p-103", d(4), erik),
        dinner("p-104", d(5), erik),

        // Kollegor på Eriks pass
        dinner("p-201", d(1), maja), dinner("p-202", d(1), omar), dinner("p-203", d(1), elin),
        dinner("p-204", d(1), johan),
        lunch("p-211", d(2), sara), lunch("p-212", d(2), maja),
        dinner("p-221", d(4), lina), dinner("p-222", d(4), omar), dinner("p-223", d(4), johan),
        dinner("p-231", d(5), sara), dinner("p-232", d(5), elin), dinner("p-233", d(5), maja),
        dinner("p-234", d(5), johan),

        // Övriga pass
        lunch("p-301", d(3), lina), dinner("p-302", d(3), omar), dinner("p-303", d(3), johan),
        lunch("p-311", d(4), lina), lunch("p-321", d(5), sara), lunch("p-331", d(6), elin),

        // Lediga pass: Johan lör 5 dec lunch, Maja fre 4 dec lunch (krockar båda med Erik, samma dag)
        lunch("p-401", d(5), johan).released("Ska på bröllop", johan, at(25, 11, 18, 40)),
        lunch("p-402", d(4), maja).released("Tandläkartid", maja, at(26, 11, 8, 15)),
        // Extra: ett pass som Erik kan ta, så att hela flödet går att prova
        dinner("p-403", d(3), maja).released("Min son är sjuk", maja, at(26, 11, 7, 30)),
        // Extra: Maja vill ge Erik sitt söndagspass -> bytesförfrågan
        lunch("p-404", d(6), maja).released(
            "Kan du ta min söndag?", maja, at(26, 11, 9, 5), ReleaseTarget.Colleague(erik.id),
        ),
    )

    fun notifications(): List<FakeNotification> = listOf(
        FakeNotification(
            erik.id,
            Notification(
                id = "n-1",
                type = NotificationType.SWAP_REQUEST,
                title = "Maja vill ge dig ett pass",
                body = "Sön 6 dec, lunch 10–15. \"Kan du ta min söndag?\"",
                createdAt = at(26, 11, 9, 5),
                read = false,
                swapId = swapIdFor("p-404"),
            ),
        ),
        FakeNotification(
            erik.id,
            Notification(
                id = "n-2",
                type = NotificationType.SHIFT_RELEASED,
                title = "Nytt ledigt pass",
                body = "Maja har lagt ut tor 3 dec, middag 16–23.",
                createdAt = at(26, 11, 7, 30),
                read = false,
            ),
        ),
        FakeNotification(
            erik.id,
            Notification(
                id = "n-3",
                type = NotificationType.INFO,
                title = "Schemat för vecka 49 är klart",
                body = "Du har fyra pass den här veckan.",
                createdAt = at(23, 11, 12, 0),
                read = true,
            ),
        ),
    )

    fun swapIdFor(shiftId: String) = "swap-$shiftId"

    private fun Shift.released(
        reason: String,
        by: Employee,
        at: Instant,
        to: ReleaseTarget = ReleaseTarget.All,
    ) = copy(
        status = ShiftStatus.RELEASED,
        releaseReason = reason,
        releasedBy = by,
        releasedAt = at,
        releasedTo = to,
    )
}

data class FakeNotification(val recipientId: String, val notification: Notification)
