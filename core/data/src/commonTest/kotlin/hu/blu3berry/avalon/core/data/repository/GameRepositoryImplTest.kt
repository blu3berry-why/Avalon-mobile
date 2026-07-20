package hu.blu3berry.avalon.core.data.repository

import app.cash.turbine.test
import hu.blu3berry.avalon.core.data.network.AvalonJson
import hu.blu3berry.avalon.core.data.network.createHttpClient
import hu.blu3berry.avalon.core.data.session.SessionManagerImpl
import hu.blu3berry.avalon.core.data.storage.InMemoryTokenStorage
import hu.blu3berry.avalon.core.domain.model.Winner
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.session.AuthEvent
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import hu.blu3berry.avalon.core.data.generated.game.Api as GameApi

/**
 * Exercises the repository through a mocked transport rather than a mocked data source, so the
 * generated client, the JSON config, and the `Either -> Result` bridge are all in the loop.
 */
class GameRepositoryImplTest {

    private fun infoJson(winner: String) = """
        {
          "started": true,
          "winner": "$winner",
          "scores": ["GOOD"],
          "currentRound": 1,
          "isAdventure": false,
          "currentAdventure": 1,
          "king": "arthur",
          "failCounter": 0,
          "selectedForAdventure": [],
          "playersName": ["arthur"],
          "assassinHasGuessed": false,
          "playerSelectNum": 2
        }
    """.trimIndent()

    /** Points the process-wide generated `Api` at [engine]. */
    private fun useEngine(engine: MockEngine, sessionManager: SessionManagerImpl) {
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

    /** Replies with [bodies] in order, repeating the last one for every further poll. */
    private fun jsonEngine(vararg bodies: String): MockEngine {
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

    @Test
    fun `getGameInfo maps the response onto the domain model`() = runTest {
        val sessionManager = SessionManagerImpl()
        useEngine(jsonEngine(infoJson("GOOD")), sessionManager)

        val result = GameRepositoryImpl(sessionManager).getGameInfo("ABCD")

        val info = assertIs<Result.Success<*, *>>(result).data
        assertEquals(Winner.GOOD, (info as hu.blu3berry.avalon.core.domain.model.GameInfo).winner)
        assertEquals(listOf("arthur"), info.players)
    }

    @Test
    fun `observeGameInfo keeps polling and only re-emits on change`() = runTest {
        val sessionManager = SessionManagerImpl()
        // First poll NOT_DECIDED, every later poll GOOD: three ticks, two distinct emissions.
        useEngine(jsonEngine(infoJson("NOT_DECIDED"), infoJson("GOOD")), sessionManager)

        val repository = GameRepositoryImpl(sessionManager, pollInterval = 10.milliseconds)

        repository.observeGameInfo("ABCD").test {
            val first = assertIs<Result.Success<*, *>>(awaitItem()).data
            assertEquals(Winner.NOT_DECIDED, (first as hu.blu3berry.avalon.core.domain.model.GameInfo).winner)

            val second = assertIs<Result.Success<*, *>>(awaitItem()).data
            assertEquals(Winner.GOOD, (second as hu.blu3berry.avalon.core.domain.model.GameInfo).winner)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed poll is emitted rather than terminating the flow`() = runTest {
        val sessionManager = SessionManagerImpl()
        var call = 0
        val engine = MockEngine {
            if (call++ == 0) {
                respondError(HttpStatusCode.ServiceUnavailable)
            } else {
                respond(
                    content = infoJson("GOOD"),
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }
        useEngine(engine, sessionManager)

        val repository = GameRepositoryImpl(sessionManager, pollInterval = 10.milliseconds)

        repository.observeGameInfo("ABCD").test {
            assertEquals(
                DataError.Network.SERVICE_UNAVAILABLE,
                assertIs<Result.Failure<*, DataError.Network>>(awaitItem()).error,
            )
            assertIs<Result.Success<*, *>>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a 401 raises a session-expired event`() = runTest {
        val sessionManager = SessionManagerImpl()
        useEngine(MockEngine { respondError(HttpStatusCode.Unauthorized) }, sessionManager)

        sessionManager.events.test {
            GameRepositoryImpl(sessionManager).getGameInfo("ABCD")
            // kmpgen's `eitherRequest` never lets a non-2xx reach the HttpClient response
            // validator — it folds the status into `Either.Left` first. The bridge in
            // `EitherToResult.kt` is therefore the only thing that reports an expired session.
            assertEquals(AuthEvent.SessionExpired, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
