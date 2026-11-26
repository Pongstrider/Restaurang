package se.storgatan.schema.data.remote

import kotlinx.serialization.Serializable
import se.storgatan.schema.domain.Employee
import se.storgatan.schema.domain.Notification
import se.storgatan.schema.domain.NotificationType
import se.storgatan.schema.domain.ReleaseTarget
import se.storgatan.schema.domain.Role
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftStatus
import se.storgatan.schema.domain.ShiftType
import se.storgatan.schema.domain.SwapRequest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val employee: EmployeeDto)

@Serializable
data class EmployeeDto(val id: String, val name: String, val role: String) {
    fun toDomain() = Employee(id, name, Role.valueOf(role))
}

@Serializable
data class ShiftDto(
    val id: String,
    val date: String,
    val type: String,
    val start: String,
    val end: String,
    val role: String,
    val employeeId: String,
    val status: String,
    val releaseReason: String? = null,
    val releasedBy: EmployeeDto? = null,
    val releasedAt: String? = null,
    val releasedTo: String? = null,
    val releasedToEmployeeId: String? = null,
    val coworkers: List<EmployeeDto> = emptyList(),
) {
    fun toDomain() = Shift(
        id = id,
        date = LocalDate.parse(date),
        type = ShiftType.valueOf(type),
        start = LocalTime.parse(start),
        end = LocalTime.parse(end),
        role = Role.valueOf(role),
        employeeId = employeeId,
        status = ShiftStatus.valueOf(status),
        releaseReason = releaseReason,
        releasedBy = releasedBy?.toDomain(),
        releasedAt = releasedAt?.let { Instant.parse(it) },
        releasedTo = when (releasedTo) {
            "ALL_SAME_ROLE" -> ReleaseTarget.AllSameRole
            "COLLEAGUE" -> releasedToEmployeeId?.let { ReleaseTarget.Colleague(it) }
            else -> null
        },
        coworkers = coworkers.map { it.toDomain() },
    )
}

@Serializable
data class ReleaseRequest(val reason: String? = null, val visibleTo: String, val employeeId: String? = null)

@Serializable
data class SwapRequestDto(
    val id: String,
    val shift: ShiftDto,
    val from: EmployeeDto,
    val createdAt: String,
) {
    fun toDomain() = SwapRequest(
        id = id,
        shift = shift.toDomain(),
        from = from.toDomain(),
        createdAt = Instant.parse(createdAt),
    )
}

@Serializable
data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val read: Boolean,
    val swapId: String? = null,
) {
    fun toDomain() = Notification(
        id = id,
        type = NotificationType.valueOf(type),
        title = title,
        body = body,
        createdAt = Instant.parse(createdAt),
        read = read,
        swapId = swapId,
    )
}

@Serializable
data class DeviceTokenRequest(val fcmToken: String)

@Serializable
data class MarkReadRequest(val ids: List<String>)
