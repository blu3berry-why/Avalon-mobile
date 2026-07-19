package hu.blu3berry.avalon.core.data.network

import hu.blu3berry.avalon.core.data.storage.TokenStorage
import hu.blu3berry.avalon.core.domain.session.AuthEvent
import hu.blu3berry.avalon.core.domain.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(
    engine: HttpClientEngine,
    tokenStorage: TokenStorage,
    sessionManager: SessionManager,
): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
    installKermitLogging()
    defaultRequest {
        header(HttpHeaders.ContentType, "application/json")
    }
    HttpResponseValidator {
        validateResponse { response: HttpResponse ->
            if (response.status == HttpStatusCode.Unauthorized) {
                sessionManager.emit(AuthEvent.LogoutRequired)
            }
        }
    }
}.also { client ->
    client.requestPipeline.intercept(HttpRequestPipeline.State) {
        tokenStorage.getToken()?.let { token ->
            context.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
