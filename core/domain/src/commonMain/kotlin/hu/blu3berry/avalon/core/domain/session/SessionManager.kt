package hu.blu3berry.avalon.core.domain.session

import kotlinx.coroutines.flow.SharedFlow

/**
 * App-wide auth-event bus. The Ktor client emits [AuthEvent.LogoutRequired] on a 401; screens
 * observe [events] to react (e.g. route back to login).
 *
 * ponytail: intentionally leaner than Re-Claw's SessionManager — no cached `currentUser` yet.
 * Add a `StateFlow<User?>` here when a screen needs the logged-in identity without a round-trip.
 */
interface SessionManager {
    val events: SharedFlow<AuthEvent>
    suspend fun emit(event: AuthEvent)
}
