package se.storgatan.schema.ui.screens.myshifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.storgatan.schema.domain.AuthRepository
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.domain.userMessage
import javax.inject.Inject

data class MyShiftsUiState(
    val shifts: List<Shift> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MyShiftsViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MyShiftsUiState())
    val state: StateFlow<MyShiftsUiState> = _state

    init {
        load()
        viewModelScope.launch {
            shiftRepository.changes.collect { load(silent = true) }
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        load(silent = true, refreshing = true)
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    private fun load(silent: Boolean = false, refreshing: Boolean = false) {
        if (!silent) _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val shifts = shiftRepository.myShifts()
                _state.update {
                    it.copy(shifts = shifts, isLoading = false, isRefreshing = false, error = null)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, isRefreshing = false, error = e.userMessage())
                }
            }
        }
    }
}
