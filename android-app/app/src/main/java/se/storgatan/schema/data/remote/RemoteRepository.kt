package se.storgatan.schema.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import retrofit2.HttpException
import se.storgatan.schema.data.session.SessionStore
import se.storgatan.schema.domain.AuthRepository
import se.storgatan.schema.domain.Employee
import se.storgatan.schema.domain.InvalidCredentialsException
import se.storgatan.schema.domain.NetworkException
import se.storgatan.schema.domain.NotFoundException
import se.storgatan.schema.domain.Notification
import se.storgatan.schema.domain.ReleaseTarget
import se.storgatan.schema.domain.Role
import se.storgatan.schema.domain.ServerException
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftConflictException
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.domain.SwapRequest
import se.storgatan.schema.domain.UnauthorizedException
import java.io.IOException

class RemoteAuthRepository(
    private val api: SchemaApi,
    private val session: SessionStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Employee = apiCall {
        val response = api.login(LoginRequest(email.trim(), password))
        val employee = response.employee.toDomain()
        session.save(response.token, employee)
        employee
    }

    override suspend fun logout() {
        session.clear()
    }
}

class RemoteShiftRepository(
    private val api: SchemaApi,
    private val session: SessionStore,
) : ShiftRepository {

    private val _changes = MutableStateFlow(0)
    override val changes: StateFlow<Int> = _changes

    private val _unreadCount = MutableStateFlow(0)
    override val unreadCount: StateFlow<Int> = _unreadCount

    override suspend fun myShifts(): List<Shift> = apiCall {
        api.myShifts().map { it.toDomain() }
    }

    override suspend fun shift(id: String): Shift = apiCall {
        api.shift(id).toDomain()
    }

    override suspend fun colleagues(role: Role): List<Employee> = apiCall {
        api.colleagues(role.name).map { it.toDomain() }
    }

    override suspend fun release(shiftId: String, reason: String?, target: ReleaseTarget): Shift = mutate {
        val body = when (target) {
            ReleaseTarget.AllSameRole -> ReleaseRequest(reason = reason, visibleTo = "ALL_SAME_ROLE")
            is ReleaseTarget.Colleague -> ReleaseRequest(reason = reason, visibleTo = "COLLEAGUE", employeeId = target.employeeId)
        }
        api.release(shiftId, body).toDomain()
    }

    override suspend fun unrelease(shiftId: String): Shift = mutate {
        api.unrelease(shiftId).toDomain()
    }

    override suspend fun releasedShifts(): List<Shift> = apiCall {
        api.releasedShifts().map { it.toDomain() }
    }

    override suspend fun claim(shiftId: String): Shift = mutate {
        api.claim(shiftId).toDomain()
    }

    override suspend fun incomingSwaps(): List<SwapRequest> = apiCall {
        api.incomingSwaps().map { it.toDomain() }
    }

    override suspend fun acceptSwap(swapId: String): Shift = mutate {
        api.acceptSwap(swapId).toDomain()
    }

    override suspend fun declineSwap(swapId: String) = mutate {
        api.declineSwap(swapId)
    }

    override suspend fun notifications(): List<Notification> = apiCall {
        api.notifications().map { it.toDomain() }.also { list ->
            _unreadCount.value = list.count { !it.read }
        }
    }

    override suspend fun markNotificationsRead(ids: List<String>) = apiCall {
        api.markNotificationsRead(MarkReadRequest(ids))
        _unreadCount.update { (it - ids.size).coerceAtLeast(0) }
    }

    override suspend fun refreshUnreadCount() {
        notifications()
    }

    override suspend fun registerDeviceToken(fcmToken: String) = apiCall {
        api.registerDeviceToken(DeviceTokenRequest(fcmToken))
    }

    private suspend fun <T> mutate(block: suspend () -> T): T =
        apiCall(block).also { _changes.update { it + 1 } }
}

private suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: HttpException) {
    when (e.code()) {
        401 -> throw UnauthorizedException()
        404 -> throw NotFoundException()
        409 -> throw ShiftConflictException()
        else -> throw ServerException(e.code())
    }
} catch (e: IOException) {
    throw NetworkException()
}
