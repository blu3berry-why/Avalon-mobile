package hu.blu3berry.avalon.core.data

import hu.blu3berry.avalon.core.data.repository.GameRepositoryImpl
import hu.blu3berry.avalon.core.data.session.SessionManagerImpl
import hu.blu3berry.avalon.core.domain.model.GameInfo
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.isActive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import hu.blu3berry.avalon.core.data.generated.game.Api as GameApi

/**
 * Guards the isolation [runApiTest] promises. Without it the process-wide generated `Api`
 * keeps the last test's client, and a later test silently runs against it.
 */
class ApiTestFixtureTest {

    private fun engineReturning(king: String) = MockEngine {
        respond(
            content = """
                {
                  "started": true,
                  "winner": "NOT_DECIDED",
                  "scores": [],
                  "currentRound": 1,
                  "isAdventure": false,
                  "currentAdventure": 1,
                  "king": "$king",
                  "failCounter": 0,
                  "selectedForAdventure": [],
                  "playersName": ["$king"],
                  "assassinHasGuessed": false,
                  "playerSelectNum": 2
                }
            """.trimIndent(),
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }

    private suspend fun readKing(): String? {
        val result = GameRepositoryImpl(SessionManagerImpl()).getGameInfo("ABCD")
        return assertIs<Result.Success<GameInfo, DataError.Network>>(result).data.king
    }

    @Test
    fun `a later test gets its own engine, not the previous one's`() {
        lateinit var firstClient: HttpClient

        runApiTest(engineReturning("arthur")) {
            firstClient = GameApi.client
            assertEquals("arthur", readKing())
        }

        runApiTest(engineReturning("mordred")) {
            assertEquals("mordred", readKing())
        }

        // The first block's client is not merely unused — it is shut down, so nothing can
        // reach the engine it wrapped.
        assertFalse(firstClient.isActive)
    }
}
