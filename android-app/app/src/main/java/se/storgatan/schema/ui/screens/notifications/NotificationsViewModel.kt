package se.storgatan.schema.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.storgatan.schema.domain.Notification
import se.storgatan.schema.domain.ShiftRepository
import se.storgatan.schema.domain.SwapRequest
import se.storgatan.schema.domain.userMessage
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val swaps: List<SwapRequest> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val processingSwapId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state

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

    fun acceptSwap(swapId: String) {
        _state.update { it.copy(processingSwapId = swapId, error = null) }
        viewModelScope.launch {
            try {
                shiftRepository.acceptSwap(swapId)
                load(silent = true)
            } catch (e: Exception) {
                _state.update { it.copy(processingSwapId = null, error = e.userMessage()) }
            }
        }
    }

    fun declineSwap(swapId: String) {
        _state.update { it.copy(processingSwapId = swapId, error = null) }
        viewModelScope.launch {
            try {
                shiftRepository.declineSwap(swapId)
                load(silent = true)
            } catch (e: Exception) {
                _state.update { it.copy(processingSwapId = null, error = e.userMessage()) }
            }
        }
    }

    private fun load(silent: Boolean = false) {
        if (!silent) _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val notifications = shiftRepository.notifications()
                val swaps = shiftRepository.incomingSwaps()
                val unreadIds = notifications.filter { !it.read }.map { it.id }
                if (unreadIds.isNotEmpty()) {
                    shiftRepository.markNotificationsRead(unreadIds)
                }
                _state.update {
                    it.copy(
                        notifications = notifications,
                        swaps = swaps,
                        isLoading = false,
                        isRefreshing = false,
                        processingSwapId = null,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, isRefreshing = false, error = e.userMessage())
                }
            }
        }
    }
}
