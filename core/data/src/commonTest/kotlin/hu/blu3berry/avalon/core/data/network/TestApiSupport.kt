package hu.blu3berry.avalon.core.data.network

import hu.blu3berry.avalon.core.data.storage.InMemoryTokenStorage
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.headersOf
import hu.blu3berry.avalon.core.data.generated.game.Api as GameApi

/**
 * The generated `Api` objects are process-wide singletons (kmpgen hardcodes `Api.client`),
 * so HTTP-level tests unavoidably mutate global state. Containment rules:
 *
 * - Only data-layer tests may touch the generated `Api` objects, and only through
 *   [useGameApiEngine], which fully re-points base URL + client at the test's own engine —
 *   no state survives from a previous test. The test task is a single sequential JVM, so
 *   there is no concurrent mutation.
 * - Screen-level tests (composeApp) must fake the repository interfaces instead of HTTP;
 *   they never load this module's test sources, let alone the generated singletons.
 */
fun useGameApiEngine(engine: MockEngine) {
    GameApi.baseUrl = Url("http://avalon.test/")
    GameApi.updateClient(
        json = AvalonJson,
        createHttpClient = { decorator ->
            createHttpClient(
                engine = engine,
                tokenStorage = InMemoryTokenStorage(),
                decorator = decorator,
            )
        },
    )
}

/** Replies with [bodies] in order, repeating the last one for every further request. */
fun jsonEngine(vararg bodies: String): MockEngine {
    var call = 0
    return MockEngine {
        val body = bodies[call.coerceAtMost(bodies.lastIndex)]
        call++
        respond(
            content = body,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }
}
