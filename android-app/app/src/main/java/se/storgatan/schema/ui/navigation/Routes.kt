package se.storgatan.schema.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object LoginRoute
@Serializable data object MyShiftsRoute
@Serializable data class ShiftDetailRoute(val shiftId: String)
@Serializable data class ReleaseShiftRoute(val shiftId: String)
@Serializable data object AvailableShiftsRoute
@Serializable data class ConfirmationRoute(val shiftId: String)
@Serializable data object NotificationsRoute
