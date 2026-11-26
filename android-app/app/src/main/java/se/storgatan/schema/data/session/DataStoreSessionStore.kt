package se.storgatan.schema.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import se.storgatan.schema.domain.Employee

private val Context.dataStore by preferencesDataStore(name = "session")

class DataStoreSessionStore(
    private val context: Context,
    scope: CoroutineScope,
) : SessionStore {

    private val tokenKey = stringPreferencesKey("token")
    private val employeeKey = stringPreferencesKey("employee")

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    override val state: StateFlow<SessionState> = _state

    init {
        scope.launch { restore() }
    }

    private suspend fun restore() {
        val prefs = context.dataStore.data.first()
        val token = prefs[tokenKey]
        val empJson = prefs[employeeKey]
        if (token != null && empJson != null) {
            val stored = Json.decodeFromString<StoredEmployee>(empJson)
            _state.value = SessionState.LoggedIn(token, stored.toDomain())
        } else {
            _state.value = SessionState.LoggedOut
        }
    }

    override suspend fun save(token: String, employee: Employee) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
            prefs[employeeKey] = Json.encodeToString(StoredEmployee.from(employee))
        }
        _state.value = SessionState.LoggedIn(token, employee)
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
        _state.value = SessionState.LoggedOut
    }

    @Serializable
    private data class StoredEmployee(val id: String, val name: String) {
        fun toDomain() = Employee(id, name)
        companion object {
            fun from(e: Employee) = StoredEmployee(e.id, e.name)
        }
    }
}
