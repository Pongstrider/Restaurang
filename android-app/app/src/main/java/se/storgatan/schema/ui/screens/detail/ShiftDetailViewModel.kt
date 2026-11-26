package se.storgatan.schema.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.storgatan.schema.data.session.SessionStore
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.domain.userMessage
import se.storgatan.schema.ui.navigation.ShiftDetailRoute
import javax.inject.Inject

data class ShiftDetailUiState(
    val shift: Shift? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isMine: Boolean = false,
    val unreleaseSuccess: Boolean = false,
    val isUnreleasing: Boolean = false,
)

@HiltViewModel
class ShiftDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shiftRepository: ShiftRepository,
    private val session: SessionStore,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ShiftDetailRoute>()
    private val _state = MutableStateFlow(ShiftDetailUiState())
    val state: StateFlow<ShiftDetailUiState> = _state

    init { load() }

    private fun load() {
        viewModelScope.launch {
            try {
                val shift = shiftRepository.shift(route.shiftId)
                val me = session.currentEmployee
                _state.update {
                    it.copy(shift = shift, isLoading = false, isMine = shift.employeeId == me?.id)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.userMessage()) }
            }
        }
    }

    fun unrelease() {
        _state.update { it.copy(isUnreleasing = true) }
        viewModelScope.launch {
            try {
                shiftRepository.unrelease(route.shiftId)
                _state.update { it.copy(isUnreleasing = false, unreleaseSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isUnreleasing = false, error = e.userMessage()) }
            }
        }
    }
}
