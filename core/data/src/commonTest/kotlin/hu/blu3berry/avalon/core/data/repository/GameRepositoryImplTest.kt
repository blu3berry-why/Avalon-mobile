package hu.blu3berry.avalon.core.data.repository

import app.cash.turbine.test
import hu.blu3berry.avalon.core.data.network.jsonEngine
import hu.blu3berry.avalon.core.data.network.useGameApiEngine
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
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

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

    @Test
    fun `getGameInfo maps the response onto the domain model`() = runTest {
        val sessionManager = SessionManagerImpl()
        useGameApiEngine(jsonEngine(infoJson("GOOD")))

        val result = GameRepositoryImpl(sessionManager).getGameInfo("ABCD")

        val info = assertIs<Result.Success<GameInfo, DataError.Network>>(result).data
        assertEquals(Winner.GOOD, info.winner)
        assertEquals(listOf("arthur"), info.players)
    }

    @Test
    fun `observeGameInfo keeps polling and only re-emits on change`() = runTest {
        val sessionManager = SessionManagerImpl()
        // First poll NOT_DECIDED, every later poll GOOD: three ticks, two distinct emissions.
        useGameApiEngine(jsonEngine(infoJson("NOT_DECIDED"), infoJson("GOOD")))

        val repository = GameRepositoryImpl(sessionManager, pollInterval = 10.milliseconds)

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
        useGameApiEngine(engine)

        val repository = GameRepositoryImpl(sessionManager, pollInterval = 10.milliseconds)

        repository.observeGameInfo("ABCD").test {
            assertEquals(
                DataError.Network.SERVICE_UNAVAILABLE,
                assertIs<Result.Failure<GameInfo, DataError.Network>>(awaitItem()).error,
            )
            assertIs<Result.Success<GameInfo, DataError.Network>>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a 401 stops the poll loop instead of re-raising the event every tick`() = runTest {
        val sessionManager = SessionManagerImpl()
        var calls = 0
        useGameApiEngine(
            MockEngine {
                calls++
                respondError(HttpStatusCode.Unauthorized)
            },
        )

        val repository = GameRepositoryImpl(sessionManager, pollInterval = 10.milliseconds)

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

    @Test
    fun `a 401 raises a session-expired event`() = runTest {
        val sessionManager = SessionManagerImpl()
        useGameApiEngine(MockEngine { respondError(HttpStatusCode.Unauthorized) })

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
