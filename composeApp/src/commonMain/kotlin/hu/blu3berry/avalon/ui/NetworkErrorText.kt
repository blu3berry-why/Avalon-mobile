package hu.blu3berry.avalon.ui

import hu.blu3berry.avalon.core.domain.result.DataError

/** Generic wording for non-auth screens; auth has its own credential-flavored mapper. */
fun DataError.Network.toUserMessage(): String = when (this) {
    DataError.Network.NOT_FOUND -> "Lobby not found — check the code"
    DataError.Network.BAD_REQUEST,
    DataError.Network.CONFLICT,
    -> "The server rejected that — the lobby may have changed"
    DataError.Network.NO_INTERNET -> "No internet connection"
    DataError.Network.REQUEST_TIMEOUT -> "The server took too long to respond"
    DataError.Network.SERVER_ERROR,
    DataError.Network.SERVICE_UNAVAILABLE,
    -> "The server is having trouble — try again later"
    else -> "Something went wrong ($name)"
}
