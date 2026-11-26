package se.storgatan.schema.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Employee(
    val id: String,
    val name: String,
)

enum class ShiftType { LUNCH, MIDDAG }

enum class ShiftStatus { ASSIGNED, RELEASED, SWAPPED }

/** Vem ett utlagt pass är synligt för. */
sealed interface ReleaseTarget {
    /** Alla kollegor. */
    data object All : ReleaseTarget

    /** En viss kollega – blir en bytesförfrågan hos henne/honom. */
    data class Colleague(val employeeId: String) : ReleaseTarget
}

data class Shift(
    val id: String,
    val date: LocalDate,
    val type: ShiftType,
    val start: LocalTime,
    val end: LocalTime,
    /** Den som har passet just nu. Ett utlagt pass är fortfarande ägarens tills någon tar det. */
    val employeeId: String,
    val status: ShiftStatus,
    val releaseReason: String? = null,
    val releasedBy: Employee? = null,
    val releasedAt: Instant? = null,
    val releasedTo: ReleaseTarget? = null,
    /** Andra som jobbar samma pass. */
    val coworkers: List<Employee> = emptyList(),
) {
    val isReleased: Boolean get() = status == ShiftStatus.RELEASED
}

/** En kollega vill ge mig ett pass (= ett pass utlagt direkt till mig). */
data class SwapRequest(
    val id: String,
    val shift: Shift,
    val from: Employee,
    val createdAt: Instant,
)

enum class NotificationType { SWAP_REQUEST, SHIFT_TAKEN, SWAP_ACCEPTED, SWAP_DECLINED, SHIFT_RELEASED, INFO }

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val read: Boolean,
    /** Satt för SWAP_REQUEST så att notisen kan visa Acceptera/Avböj. */
    val swapId: String? = null,
)
