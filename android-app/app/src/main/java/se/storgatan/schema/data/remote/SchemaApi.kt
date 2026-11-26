package se.storgatan.schema.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SchemaApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("me/shifts")
    suspend fun myShifts(): List<ShiftDto>

    @GET("shifts/{id}")
    suspend fun shift(@Path("id") id: String): ShiftDto

    @GET("colleagues")
    suspend fun colleagues(): List<EmployeeDto>

    @POST("shifts/{id}/release")
    suspend fun release(@Path("id") id: String, @Body body: ReleaseRequest): ShiftDto

    @POST("shifts/{id}/unrelease")
    suspend fun unrelease(@Path("id") id: String): ShiftDto

    @GET("shifts/released")
    suspend fun releasedShifts(): List<ShiftDto>

    @POST("shifts/{id}/claim")
    suspend fun claim(@Path("id") id: String): ShiftDto

    @GET("swaps/incoming")
    suspend fun incomingSwaps(): List<SwapRequestDto>

    @POST("swaps/{id}/accept")
    suspend fun acceptSwap(@Path("id") id: String): ShiftDto

    @POST("swaps/{id}/decline")
    suspend fun declineSwap(@Path("id") id: String)

    @GET("me/notifications")
    suspend fun notifications(): List<NotificationDto>

    @POST("me/notifications/read")
    suspend fun markNotificationsRead(@Body body: MarkReadRequest)

    @POST("me/device-token")
    suspend fun registerDeviceToken(@Body body: DeviceTokenRequest)
}
