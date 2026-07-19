package hu.blu3berry.avalon.core.data.session

import hu.blu3berry.avalon.core.domain.session.AuthEvent
import hu.blu3berry.avalon.core.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SessionManagerImpl : SessionManager {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    override suspend fun emit(event: AuthEvent) { _events.tryEmit(event) }
}
