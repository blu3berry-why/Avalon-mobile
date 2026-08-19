package hu.blu3berry.avalon.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI base: screens render [state], dispatch [onAction], and collect one-shot [events]
 * (navigation, snackbars — things that must not replay on recomposition).
 *
 * Mirror note: this shape mirrors Re-Claw's presentation scaffolding. The Re-Claw
 * reference was unreachable from the session that authored this, so it is the standard
 * State/Action/Event shape — sync upstream before diverging further.
 */
abstract class MviViewModel<STATE, ACTION, EVENT>(initialState: STATE) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _events = Channel<EVENT>(Channel.BUFFERED)
    val events: Flow<EVENT> = _events.receiveAsFlow()

    abstract fun onAction(action: ACTION)

    protected fun updateState(reduce: STATE.() -> STATE) = _state.update(reduce)

    protected fun sendEvent(event: EVENT) {
        viewModelScope.launch { _events.send(event) }
    }
}
