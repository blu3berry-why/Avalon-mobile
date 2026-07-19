package hu.blu3berry.avalon.core.domain.session

sealed interface AuthEvent {
    /** Manual logout, or the app force-clearing the session. */
    data object LogoutRequired : AuthEvent

    /** Backend rejected the bearer token (401). The auth screen shows a one-time
     *  "session expired" message in response. */
    data object SessionExpired : AuthEvent
}
