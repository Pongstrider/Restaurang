package se.storgatan.schema.ui.screens.available

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftConflicts
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.domain.userMessage
import javax.inject.Inject

data class AvailableShiftItem(
    val shift: Shift,
    val conflictsWithMine: Boolean,
    val conflictingShift: Shift? = null,
)

data class AvailableUiState(
    val items: List<AvailableShiftItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val claimingId: String? = null,
    val error: String? = null,
    val claimedShiftId: String? = null,
)

@HiltViewModel
class AvailableShiftsViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AvailableUiState())
    val state: StateFlow<AvailableUiState> = _state

    init {
        load()
        viewModelScope.launch {
            shiftRepository.changes.collect { load(silent = true) }
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        load(silent = true)
    }

    fun claim(shiftId: String) {
        _state.update { it.copy(claimingId = shiftId, error = null) }
        viewModelScope.launch {
            try {
                shiftRepository.claim(shiftId)
                _state.update { it.copy(claimingId = null, claimedShiftId = shiftId) }
            } catch (e: Exception) {
                _state.update { it.copy(claimingId = null, error = e.userMessage()) }
            }
        }
    }

    fun clearClaimed() {
        _state.update { it.copy(claimedShiftId = null) }
    }

    private fun load(silent: Boolean = false) {
        if (!silent) _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val released = shiftRepository.releasedShifts()
                val myShifts = shiftRepository.myShifts()
                val items = released.map { shift ->
                    val conflict = ShiftConflicts.findConflict(shift, myShifts)
                    AvailableShiftItem(shift, conflict != null, conflict)
                }
                _state.update {
                    it.copy(items = items, isLoading = false, isRefreshing = false, error = null)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = e.userMessage()) }
            }
        }
    }
}
