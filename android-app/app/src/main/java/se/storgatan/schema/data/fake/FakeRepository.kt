package se.storgatan.schema.data.fake

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.storgatan.schema.data.session.SessionStore
import se.storgatan.schema.domain.AuthRepository
import se.storgatan.schema.domain.Employee
import se.storgatan.schema.domain.InvalidCredentialsException
import se.storgatan.schema.domain.NotFoundException
import se.storgatan.schema.domain.Notification
import se.storgatan.schema.domain.NotificationType
import se.storgatan.schema.domain.ReleaseTarget
import se.storgatan.schema.domain.Role
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftConflictException
import se.storgatan.schema.domain.ShiftConflicts
import se.storgatan.schema.domain.ShiftFormat
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.domain.ShiftStatus
import se.storgatan.schema.domain.SwapRequest
import se.storgatan.schema.domain.UnauthorizedException
import java.time.Clock
import java.util.Locale

/**
 * Låtsas-backend som håller all state i minnet, så att det man gör i appen syns direkt.
 * Varje anrop väntar [latencyMs] för att kännas som nätverk.
 * Ett pass utlagt till en viss kollega är samtidigt en bytesförfrågan hos den kollegan.
 */
class FakeRepository(
    private val session: SessionStore,
    private val clock: Clock = Clock.system(FakeData.ZONE),
    private val latencyMs: Long = 600,
) : AuthRepository, ShiftRepository {

    private val mutex = Mutex()
    private val employees: Map<String, Employee> = FakeData.employees.associateBy { it.id }
    private val shifts: MutableList<Shift> = FakeData.shifts().toMutableList()
    private val notifications: MutableList<FakeNotification> = FakeData.notifications().toMutableList()
    private var nextId = 1000

    private val _changes = MutableStateFlow(0)
    override val changes: StateFlow<Int> = _changes

    private val _unreadCount = MutableStateFlow(0)
    override val unreadCount: StateFlow<Int> = _unreadCount

    // ---- Auth ----

    override suspend fun login(email: String, password: String): Employee {
        delay(latencyMs)
        val normalized = email.trim().lowercase(Locale.ROOT)
        val employee = FakeData.employees.firstOrNull { FakeData.emailFor(it) == normalized }
        if (employee == null || password != FakeData.PASSWORD) throw InvalidCredentialsException()
        session.save("fake-token-${employee.id}", employee)
        mutex.withLock { updateUnread(employee.id) }
        return employee
    }

    override suspend fun logout() {
        session.clear()
        _unreadCount.value = 0
    }

    // ---- Pass ----

    override suspend fun myShifts(): List<Shift> = call { me ->
        shifts.filter { it.employeeId == me.id }.sortedWith(SHIFT_ORDER).map { it.withCoworkers() }
    }

    override suspend fun shift(id: String): Shift = call { findShift(id).withCoworkers() }

    override suspend fun colleagues(role: Role): List<Employee> = call { me ->
        FakeData.employees.filter { it.role == role && it.id != me.id }
    }

    override suspend fun release(shiftId: String, reason: String?, target: ReleaseTarget): Shift = mutate { me ->
        val shift = findShift(shiftId)
        if (shift.employeeId != me.id) throw ShiftConflictException("Det här passet är inte ditt.")
        if (shift.isReleased) throw ShiftConflictException("Passet är redan utlagt.")
        if (target is ReleaseTarget.Colleague) {
            if (target.employeeId == me.id || target.employeeId !in employees) throw NotFoundException("Hittade inte kollegan.")
        }
        val updated = shift.copy(
            status = ShiftStatus.RELEASED,
            releaseReason = reason?.trim()?.takeIf { it.isNotEmpty() },
            releasedBy = me,
            releasedAt = clock.instant(),
            releasedTo = target,
        )
        replace(updated)
        when (target) {
            is ReleaseTarget.Colleague -> notify(
                target.employeeId,
                NotificationType.SWAP_REQUEST,
                "${me.name} vill ge dig ett pass",
                describe(updated) + (updated.releaseReason?.let { " \"$it\"" } ?: ""),
                swapId = FakeData.swapIdFor(updated.id),
            )
            ReleaseTarget.AllSameRole -> employees.values
                .filter { it.role == updated.role && it.id != me.id }
                .forEach { notify(it.id, NotificationType.SHIFT_RELEASED, "Nytt ledigt pass", "${me.name} har lagt ut ${describe(updated)}.") }
        }
        updated.withCoworkers()
    }

    override suspend fun unrelease(shiftId: String): Shift = mutate { me ->
        val shift = findShift(shiftId)
        if (shift.employeeId != me.id || !shift.isReleased) {
            throw ShiftConflictException("Passet kan inte ångras – någon har redan tagit det.")
        }
        val updated = shift.clearRelease(ShiftStatus.ASSIGNED)
        replace(updated)
        updated.withCoworkers()
    }

    override suspend fun releasedShifts(): List<Shift> = call { me ->
        shifts.filter { it.isReleased && it.employeeId != me.id && it.isVisibleTo(me) }
            .sortedWith(SHIFT_ORDER)
            .map { it.withCoworkers() }
    }

    override suspend fun claim(shiftId: String): Shift = mutate { me -> claimLocked(me, shiftId) }

    // ---- Byten ----

    override suspend fun incomingSwaps(): List<SwapRequest> = call { me ->
        shifts.filter { it.isReleased && it.releasedTo == ReleaseTarget.Colleague(me.id) }
            .sortedWith(SHIFT_ORDER)
            .map { shift ->
                SwapRequest(
                    id = FakeData.swapIdFor(shift.id),
                    shift = shift.withCoworkers(),
                    from = shift.releasedBy ?: employees.getValue(shift.employeeId),
                    createdAt = shift.releasedAt ?: clock.instant(),
                )
            }
    }

    override suspend fun acceptSwap(swapId: String): Shift = mutate { me ->
        claimLocked(me, pendingSwapShift(me, swapId).id)
    }

    override suspend fun declineSwap(swapId: String) = mutate { me ->
        val shift = pendingSwapShift(me, swapId)
        replace(shift.clearRelease(ShiftStatus.ASSIGNED))
        notify(shift.employeeId, NotificationType.SWAP_DECLINED, "${me.name} kunde inte ta passet", "${describe(shift)} är fortfarande ditt.")
    }

    // ---- Notiser ----

    override suspend fun notifications(): List<Notification> = call { me ->
        updateUnread(me.id)
        notifications.filter { it.recipientId == me.id }.map { it.notification }.sortedByDescending { it.createdAt }
    }

    override suspend fun markNotificationsRead(ids: List<String>) = call { me ->
        notifications.replaceAll { n ->
            if (n.recipientId == me.id && n.notification.id in ids) n.copy(notification = n.notification.copy(read = true)) else n
        }
        updateUnread(me.id)
    }

    override suspend fun refreshUnreadCount() = call { me -> updateUnread(me.id) }

    override suspend fun registerDeviceToken(fcmToken: String) = call { }

    // ---- Hjälpare (anropas med mutex låst) ----

    private suspend fun <T> call(block: (me: Employee) -> T): T {
        delay(latencyMs)
        return mutex.withLock { block(currentUser()) }
    }

    private suspend fun <T> mutate(block: (me: Employee) -> T): T =
        call(block).also { _changes.update { it + 1 } }

    private fun currentUser(): Employee = session.currentEmployee ?: throw UnauthorizedException()

    private fun claimLocked(me: Employee, shiftId: String): Shift {
        val shift = findShift(shiftId)
        if (!shift.isReleased || shift.employeeId == me.id || !shift.isVisibleTo(me)) {
            throw ShiftConflictException("Passet är inte längre ledigt – någon hann före.")
        }
        val mine = shifts.filter { it.employeeId == me.id }
        if (ShiftConflicts.conflicts(shift, mine)) {
            throw ShiftConflictException("Krockar med ditt pass samma dag.")
        }
        val previousOwner = shift.employeeId
        val updated = shift.clearRelease(ShiftStatus.SWAPPED).copy(employeeId = me.id)
        replace(updated)
        notify(previousOwner, NotificationType.SHIFT_TAKEN, "${me.name} tog ditt pass", "${describe(updated)} är inte längre ditt.")
        notify(FakeData.MANAGER_ID, NotificationType.SHIFT_TAKEN, "Pass har bytt person", "${describe(updated)}: ${employees[previousOwner]?.name} → ${me.name}.")
        return updated.withCoworkers()
    }

    private fun pendingSwapShift(me: Employee, swapId: String): Shift =
        shifts.firstOrNull {
            FakeData.swapIdFor(it.id) == swapId && it.isReleased && it.releasedTo == ReleaseTarget.Colleague(me.id)
        } ?: throw ShiftConflictException("Förfrågan gäller inte längre.")

    private fun findShift(id: String): Shift = shifts.firstOrNull { it.id == id } ?: throw NotFoundException()

    private fun replace(updated: Shift) {
        shifts[shifts.indexOfFirst { it.id == updated.id }] = updated
    }

    private fun Shift.isVisibleTo(me: Employee): Boolean = when (val to = releasedTo) {
        ReleaseTarget.AllSameRole, null -> true
        is ReleaseTarget.Colleague -> to.employeeId == me.id
    }

    private fun Shift.withCoworkers(): Shift = copy(
        coworkers = shifts
            .filter { it.date == date && it.type == type && it.employeeId != employeeId }
            .mapNotNull { employees[it.employeeId] }
            .distinctBy { it.id },
    )

    private fun Shift.clearRelease(newStatus: ShiftStatus) = copy(
        status = newStatus,
        releaseReason = null,
        releasedBy = null,
        releasedAt = null,
        releasedTo = null,
    )

    private fun notify(recipientId: String, type: NotificationType, title: String, body: String, swapId: String? = null) {
        val n = Notification("n-${nextId++}", type, title, body, clock.instant(), read = false, swapId = swapId)
        notifications += FakeNotification(recipientId, n)
        session.currentEmployee?.let { updateUnread(it.id) }
    }

    private fun updateUnread(meId: String) {
        _unreadCount.value = notifications.count { it.recipientId == meId && !it.notification.read }
    }

    private fun describe(shift: Shift): String =
        ShiftFormat.describe(shift).replaceFirstChar { it.uppercase() }

    private companion object {
        val SHIFT_ORDER = compareBy<Shift>({ it.date }, { it.start })
    }
}
