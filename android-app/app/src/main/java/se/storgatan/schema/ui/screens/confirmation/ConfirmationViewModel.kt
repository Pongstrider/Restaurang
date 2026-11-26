package se.storgatan.schema.ui.screens.confirmation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.ui.navigation.ConfirmationRoute
import javax.inject.Inject

@HiltViewModel
class ConfirmationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shiftRepository: ShiftRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ConfirmationRoute>()
    private val _shift = MutableStateFlow<Shift?>(null)
    val shift: StateFlow<Shift?> = _shift

    init {
        viewModelScope.launch {
            try { _shift.value = shiftRepository.shift(route.shiftId) } catch (_: Exception) {}
        }
    }
}
