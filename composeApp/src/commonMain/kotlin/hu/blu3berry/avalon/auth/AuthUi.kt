package hu.blu3berry.avalon.auth

import hu.blu3berry.avalon.core.domain.result.DataError

/** Auth-screen wording for network failures; login treats 401 as bad credentials. */
fun DataError.Network.toAuthMessage(): String = when (this) {
    DataError.Network.UNAUTHORIZED -> "Invalid username or password"
    DataError.Network.CONFLICT -> "That username is already taken"
    DataError.Network.NO_INTERNET -> "No internet connection"
    DataError.Network.REQUEST_TIMEOUT -> "The server took too long to respond"
    DataError.Network.SERVER_ERROR,
    DataError.Network.SERVICE_UNAVAILABLE,
    -> "The server is having trouble — try again later"
    else -> "Something went wrong ($name)"
}
