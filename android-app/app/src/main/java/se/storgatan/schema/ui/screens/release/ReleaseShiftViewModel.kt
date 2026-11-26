package se.storgatan.schema.ui.screens.release

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.storgatan.schema.domain.Employee
import se.storgatan.schema.domain.ReleaseTarget
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.domain.userMessage
import se.storgatan.schema.ui.navigation.ReleaseShiftRoute
import javax.inject.Inject

data class ReleaseUiState(
    val shift: Shift? = null,
    val colleagues: List<Employee> = emptyList(),
    val reason: String = "",
    val targetAll: Boolean = true,
    val selectedColleague: Employee? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class ReleaseShiftViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shiftRepository: ShiftRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ReleaseShiftRoute>()
    private val _state = MutableStateFlow(ReleaseUiState())
    val state: StateFlow<ReleaseUiState> = _state

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val shift = shiftRepository.shift(route.shiftId)
                val colleagues = shiftRepository.colleagues(shift.role)
                _state.update { it.copy(shift = shift, colleagues = colleagues, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.userMessage()) }
            }
        }
    }

    fun onReasonChange(value: String) = _state.update { it.copy(reason = value) }
    fun selectAll() = _state.update { it.copy(targetAll = true, selectedColleague = null) }
    fun selectColleague(c: Employee) = _state.update { it.copy(targetAll = false, selectedColleague = c) }

    fun release() {
        val current = _state.value
        val target = if (current.targetAll) {
            ReleaseTarget.AllSameRole
        } else {
            val c = current.selectedColleague ?: return
            ReleaseTarget.Colleague(c.id)
        }
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                shiftRepository.release(route.shiftId, current.reason.takeIf { it.isNotBlank() }, target)
                _state.update { it.copy(isSubmitting = false, success = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false, error = e.userMessage()) }
            }
        }
    }
}
