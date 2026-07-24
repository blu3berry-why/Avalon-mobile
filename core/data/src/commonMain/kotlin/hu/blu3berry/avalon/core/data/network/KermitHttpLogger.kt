package hu.blu3berry.avalon.core.data.network

import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders

/**
 * Routes Ktor `Logging` plugin output through Kermit so requests/responses appear in the
 * platform's standard log sink (Logcat on Android, console on Desktop, NSLog on iOS) under the
 * "HttpClient" tag.
 */
object KermitHttpLogger : Logger {
    private val logger = KermitLogger.withTag("HttpClient")
    override fun log(message: String) {
        logger.i { message }
    }
}

/**
 * Installs Ktor's `Logging` plugin wired to [KermitHttpLogger] with `LogLevel.ALL` and the
 * `Authorization` header sanitized. Reused by the kmpgen-managed `Api` client in batch 2 via
 * `Api.updateClient(decorator = ::installKermitLogging)`.
 */
fun HttpClientConfig<*>.installKermitLogging() {
    install(Logging) {
        logger = KermitHttpLogger
        level = LogLevel.ALL
        sanitizeHeader { name -> name == HttpHeaders.Authorization }
    }
}
