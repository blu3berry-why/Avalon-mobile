package hu.blu3berry.avalon.core.data.network

import hu.blu3berry.avalon.core.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

/**
 * The JSON shape every API speaks. `ignoreUnknownKeys` so a server-side field addition is not
 * a client crash; `explicitNulls = false` so absent optional fields serialize as omitted
 * rather than `null` (the server treats the two differently on `LoginInfo` updates).
 */
val AvalonJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * Builds the client the kmpgen `Api` singletons run on. Passed to `ApiHolder.updateClient` as
 * its `createHttpClient` argument, so [decorator] carries kmpgen's own config (content
 * negotiation, base URL, auth plugin) and must be applied.
 *
 * The one Avalon-specific concern here is the bearer token, appended per-request because it
 * changes on login/logout and the game API declares no security scheme for kmpgen's auth
 * plugin to hang off.
 *
 * 401 handling deliberately lives elsewhere: kmpgen's `eitherRequest` folds a non-2xx status
 * into `Either.Left` before the response validator would see it, so `EitherToResult.kt` is
 * what raises [hu.blu3berry.avalon.core.domain.session.AuthEvent.SessionExpired].
 */
fun createHttpClient(
    engine: HttpClientEngine,
    tokenStorage: TokenStorage,
    decorator: HttpClientConfig<*>.() -> Unit = {},
): HttpClient = HttpClient(engine) {
    installKermitLogging()
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
    }
    decorator()
}.also { client ->
    client.requestPipeline.intercept(HttpRequestPipeline.State) {
        tokenStorage.getToken()?.let { token ->
            // Replace rather than append: the pipeline runs again for the same request builder
            // on a retry, and two Authorization headers is a 400 from most gateways.
            context.headers.remove(HttpHeaders.Authorization)
            context.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}

/**
 * Without these a request can hang indefinitely — which for `GameRepositoryImpl`'s poll loop
 * means the loop stops ticking with no error and no way for the UI to notice.
 */
private const val REQUEST_TIMEOUT_MILLIS = 30_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val SOCKET_TIMEOUT_MILLIS = 30_000L
