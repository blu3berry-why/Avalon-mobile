package hu.blu3berry.avalon.core.data.network

import arrow.core.Either
import com.kroegerama.openapi.kmp.gen.companion.CallException
import com.kroegerama.openapi.kmp.gen.companion.HttpCallException
import com.kroegerama.openapi.kmp.gen.companion.HttpCallResponse
import com.kroegerama.openapi.kmp.gen.companion.IOCallException
import com.kroegerama.openapi.kmp.gen.companion.SerializationException
import com.kroegerama.openapi.kmp.gen.companion.UnexpectedCallException
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.session.AuthEvent
import hu.blu3berry.avalon.core.domain.session.SessionManager

/**
 * Bridge from kmpgen-generated responses (`Either<CallException, HttpCallResponse<D>>`) to the
 * domain `Result`. Every repository goes through this, which keeps arrow + the kmpgen
 * companion types out of the domain layer.
 */
fun <D> Either<CallException, HttpCallResponse<D>>.toResult(): Result<D, DataError.Network> =
    fold(
        ifLeft = { exception -> Result.Failure(exception.toDataError()) },
        ifRight = { response -> Result.Success(response.data) },
    )

/**
 * A 401 here means the gateway rejected the JWT (expired, or revoked with the account). The
 * `HttpClient` validator covers responses that go through it; kmpgen calls surface the status
 * as a `Left` instead, so repositories fan the same event out from this helper.
 */
suspend fun <D> Result<D, DataError.Network>.emitLogoutOnUnauthorized(
    sessionManager: SessionManager,
): Result<D, DataError.Network> = also {
    if (this is Result.Failure && error == DataError.Network.UNAUTHORIZED) {
        sessionManager.emit(AuthEvent.SessionExpired)
    }
}

private fun CallException.toDataError(): DataError.Network = when (this) {
    is HttpCallException -> when (code) {
        400 -> DataError.Network.BAD_REQUEST
        401 -> DataError.Network.UNAUTHORIZED
        403 -> DataError.Network.FORBIDDEN
        404 -> DataError.Network.NOT_FOUND
        408 -> DataError.Network.REQUEST_TIMEOUT
        409 -> DataError.Network.CONFLICT
        413 -> DataError.Network.PAYLOAD_TOO_LARGE
        429 -> DataError.Network.TOO_MANY_REQUESTS
        500 -> DataError.Network.SERVER_ERROR
        503 -> DataError.Network.SERVICE_UNAVAILABLE
        else -> DataError.Network.UNKNOWN
    }
    is IOCallException -> DataError.Network.NO_INTERNET
    is SerializationException -> DataError.Network.SERIALIZATION
    is UnexpectedCallException -> DataError.Network.UNKNOWN
}
