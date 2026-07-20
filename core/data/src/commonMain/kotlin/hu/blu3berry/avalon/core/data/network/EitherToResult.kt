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
 * The one call every repository method makes: bridges a kmpgen response
 * (`Either<CallException, HttpCallResponse<D>>`) onto the domain `Result`, reports an expired
 * session, and maps the payload onto its domain type.
 *
 * Keeping the three steps together is what stops arrow and the kmpgen companion types from
 * leaking past this file, and means no call site can forget the 401 handling.
 *
 * A 401 means the gateway rejected the JWT (expired, or revoked with the account). kmpgen's
 * `eitherRequest` folds a non-2xx into `Left` before the `HttpClient` response validator would
 * see it, so this is the only place that can raise the event.
 */
suspend fun <D, R> Either<CallException, HttpCallResponse<D>>.toResult(
    sessionManager: SessionManager,
    transform: (D) -> R,
): Result<R, DataError.Network> = when (this) {
    is Either.Right -> Result.Success(transform(value.data))
    is Either.Left -> {
        val error = value.toDataError()
        if (error == DataError.Network.UNAUTHORIZED) {
            sessionManager.emit(AuthEvent.SessionExpired)
        }
        Result.Failure(error)
    }
}

/** [toResult] for responses whose payload needs no mapping (or is discarded). */
suspend fun <D> Either<CallException, HttpCallResponse<D>>.toResult(
    sessionManager: SessionManager,
): Result<D, DataError.Network> = toResult(sessionManager) { it }

/**
 * The bridge without session handling, for the auth service: a 401 from `/login` means "wrong
 * password", and raising a session-expired event on it would be wrong.
 */
fun <D> Either<CallException, HttpCallResponse<D>>.toResult(): Result<D, DataError.Network> =
    fold(
        ifLeft = { exception -> Result.Failure(exception.toDataError()) },
        ifRight = { response -> Result.Success(response.data) },
    )

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
