package se.storgatan.schema.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Inloggning. Implementeras av FakeRepository (testdata) och RemoteRepository (REST).
 * Sessionen (token + inloggad person) sparas i SessionStore.
 */
interface AuthRepository {
    /** Loggar in och sparar sessionen. Kastar [InvalidCredentialsException] vid fel uppgifter. */
    suspend fun login(email: String, password: String): Employee

    suspend fun logout()
}

/**
 * Allt som rör pass, byten och notiser. Alla anrop kan kasta [AppException].
 */
interface ShiftRepository {
    /** Ökar varje gång ett pass ändras via appen, så att skärmar kan ladda om. */
    val changes: StateFlow<Int>

    /** Antal olästa notiser, för badgen i bottenmenyn. */
    val unreadCount: StateFlow<Int>

    /** Mina kommande pass, sorterade, med kollegor på samma pass. */
    suspend fun myShifts(): List<Shift>

    suspend fun shift(id: String): Shift

    /** Övriga anställda (inte jag själv), för "En viss kollega". */
    suspend fun colleagues(): List<Employee>

    suspend fun release(shiftId: String, reason: String?, target: ReleaseTarget): Shift

    /** Ångrar ett utlagt pass. Kastar [ShiftConflictException] om någon redan tagit det. */
    suspend fun unrelease(shiftId: String): Shift

    /** Lediga pass som jag kan se (utlagda till alla eller till mig). */
    suspend fun releasedShifts(): List<Shift>

    /** Tar ett pass. Kastar [ShiftConflictException] (409) vid krock eller om det redan är taget. */
    suspend fun claim(shiftId: String): Shift

    suspend fun incomingSwaps(): List<SwapRequest>

    suspend fun acceptSwap(swapId: String): Shift

    suspend fun declineSwap(swapId: String)

    suspend fun notifications(): List<Notification>

    suspend fun markNotificationsRead(ids: List<String>)

    suspend fun refreshUnreadCount()

    /** För push-notiser (FCM) – förberett, används inte än. */
    suspend fun registerDeviceToken(fcmToken: String)
}
