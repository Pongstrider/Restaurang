package se.storgatan.schema.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import se.storgatan.schema.domain.Employee

sealed interface SessionState {
    /** Läser sparad session vid appstart. */
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val token: String, val employee: Employee) : SessionState
}

interface SessionStore {
    val state: StateFlow<SessionState>

    /** Synkron åtkomst för OkHttp-interceptorn. */
    val token: String? get() = (state.value as? SessionState.LoggedIn)?.token

    val currentEmployee: Employee? get() = (state.value as? SessionState.LoggedIn)?.employee

    suspend fun save(token: String, employee: Employee)

    suspend fun clear()
}

/** Används i tester. */
class InMemorySessionStore(initial: SessionState = SessionState.LoggedOut) : SessionStore {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<SessionState> = _state

    override suspend fun save(token: String, employee: Employee) {
        _state.value = SessionState.LoggedIn(token, employee)
    }

    override suspend fun clear() {
        _state.value = SessionState.LoggedOut
    }
}
