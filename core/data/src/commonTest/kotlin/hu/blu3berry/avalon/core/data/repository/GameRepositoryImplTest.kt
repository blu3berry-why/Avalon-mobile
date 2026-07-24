package hu.blu3berry.avalon.core.data.repository

import app.cash.turbine.test
import hu.blu3berry.avalon.core.data.runApiTest
import hu.blu3berry.avalon.core.data.session.SessionManagerImpl
import hu.blu3berry.avalon.core.domain.model.GameInfo
import hu.blu3berry.avalon.core.domain.model.Winner
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.session.AuthEvent
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.TestResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

/**
 * Exercises the repository through a mocked transport rather than a mocked data source, so the
 * generated client, the JSON config, and the `Either -> Result` bridge are all in the loop.
 *
 * The generated API is a process-wide singleton — see `ApiTestFixture.kt` for why every test
 * here goes through [runApiTest].
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

    private fun jsonResponse(body: String) = body to headersOf(
        HttpHeaders.ContentType,
        ContentType.Application.Json.toString(),
    )

    /** Replies with [bodies] in order, repeating the last one for every further poll. */
    private fun jsonEngine(vararg bodies: String): MockEngine {
        var call = 0
        return MockEngine {
            val (body, headers) = jsonResponse(bodies[call.coerceAtMost(bodies.lastIndex)])
            call++
            respond(content = body, headers = headers)
        }
    }

    @Test
    fun `getGameInfo maps the response onto the domain model`() = runApiTest(jsonEngine(infoJson("GOOD"))) {
        val result = GameRepositoryImpl(SessionManagerImpl()).getGameInfo("ABCD")

        val info = assertIs<Result.Success<GameInfo, DataError.Network>>(result).data
        assertEquals(Winner.GOOD, info.winner)
        assertEquals(listOf("arthur"), info.players)
    }

    @Test
    fun `observeGameInfo keeps polling and only re-emits on change`() =
        // First poll NOT_DECIDED, every later poll GOOD: three ticks, two distinct emissions.
        runApiTest(jsonEngine(infoJson("NOT_DECIDED"), infoJson("GOOD"))) {
            val repository = GameRepositoryImpl(SessionManagerImpl(), pollInterval = 10.milliseconds)

            repository.observeGameInfo("ABCD").test {
                val first = assertIs<Result.Success<GameInfo, DataError.Network>>(awaitItem()).data
                assertEquals(Winner.NOT_DECIDED, first.winner)

                val second = assertIs<Result.Success<GameInfo, DataError.Network>>(awaitItem()).data
                assertEquals(Winner.GOOD, second.winner)

                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `a failed poll is emitted rather than terminating the flow`(): TestResult {
        var call = 0
        val engine = MockEngine {
            if (call++ == 0) {
                respondError(HttpStatusCode.ServiceUnavailable)
            } else {
                val (body, headers) = jsonResponse(infoJson("GOOD"))
                respond(content = body, headers = headers)
            }
        }
        return runApiTest(engine) {
            val repository = GameRepositoryImpl(SessionManagerImpl(), pollInterval = 10.milliseconds)

            repository.observeGameInfo("ABCD").test {
                assertEquals(
                    DataError.Network.SERVICE_UNAVAILABLE,
                    assertIs<Result.Failure<GameInfo, DataError.Network>>(awaitItem()).error,
                )
                assertIs<Result.Success<GameInfo, DataError.Network>>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `a 401 stops the poll loop instead of re-raising the event every tick`(): TestResult {
        var calls = 0
        val engine = MockEngine {
            calls++
            respondError(HttpStatusCode.Unauthorized)
        }
        return runApiTest(engine) {
            val repository = GameRepositoryImpl(SessionManagerImpl(), pollInterval = 10.milliseconds)

            repository.observeGameInfo("ABCD").test {
                assertEquals(
                    DataError.Network.UNAUTHORIZED,
                    assertIs<Result.Failure<GameInfo, DataError.Network>>(awaitItem()).error,
                )
                // The flow completes rather than polling on with a token the gateway has rejected.
                awaitComplete()
            }
            assertEquals(1, calls)
        }
    }

    @Test
    fun `a 401 raises a session-expired event`() =
        runApiTest(MockEngine { respondError(HttpStatusCode.Unauthorized) }) {
            val sessionManager = SessionManagerImpl()

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
