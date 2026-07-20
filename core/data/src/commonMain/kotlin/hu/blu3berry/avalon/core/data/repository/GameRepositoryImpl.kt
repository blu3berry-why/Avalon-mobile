package hu.blu3berry.avalon.core.data.repository

import hu.blu3berry.avalon.core.data.generated.game.api.GameControllerApi
import hu.blu3berry.avalon.core.data.generated.game.models.AssassinGuess
import hu.blu3berry.avalon.core.data.generated.game.models.SingleVote
import hu.blu3berry.avalon.core.data.generated.game.models.generated.*
import hu.blu3berry.avalon.core.data.network.toResult
import hu.blu3berry.avalon.core.domain.model.Character
import hu.blu3berry.avalon.core.domain.model.GameInfo
import hu.blu3berry.avalon.core.domain.repository.GameRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.result.asEmptyDataResult
import hu.blu3berry.avalon.core.domain.session.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @param pollInterval how long to wait between game-state reads. Injectable so tests don't
 *   have to sit through [DEFAULT_POLL_INTERVAL].
 */
class GameRepositoryImpl(
    private val sessionManager: SessionManager,
    private val pollInterval: Duration = DEFAULT_POLL_INTERVAL,
) : GameRepository {

    /**
     * ponytail: polling loop, because the server exposes no push channel. Swap the flow body
     * for a socket subscription when one exists (D1) — callers see no change.
     *
     * Errors are emitted, not thrown: a single failed poll (tunnel hiccup, backgrounded app)
     * must not tear down the collector, and `distinctUntilChanged` keeps a persistent failure
     * from re-emitting on every tick.
     */
    override fun observeGameInfo(lobbyCode: String): Flow<Result<GameInfo, DataError.Network>> =
        flow {
            while (true) {
                emit(getGameInfo(lobbyCode))
                delay(pollInterval)
            }
        }.distinctUntilChanged()

    override suspend fun getGameInfo(lobbyCode: String): Result<GameInfo, DataError.Network> =
        GameControllerApi.getGameInfo(lobbyCode = lobbyCode)
            .toResult(sessionManager) { dto -> dto.toDomain() }

    override suspend fun getCharacter(lobbyCode: String): Result<Character, DataError.Network> =
        GameControllerApi.getCharacter(lobbyCode = lobbyCode)
            .toResult(sessionManager) { dto -> dto.toDomain() }

    override suspend fun voteOnTeam(
        lobbyCode: String,
        username: String,
        approve: Boolean,
    ): EmptyResult<DataError.Network> =
        GameControllerApi.vote(
            lobbyCode = lobbyCode,
            body = SingleVote(username = username, uservote = approve),
        )
            .toResult(sessionManager)
            .asEmptyDataResult()

    override suspend fun voteOnAdventure(
        lobbyCode: String,
        username: String,
        succeed: Boolean,
    ): EmptyResult<DataError.Network> =
        GameControllerApi.adventure(
            lobbyCode = lobbyCode,
            body = SingleVote(username = username, uservote = succeed),
        )
            .toResult(sessionManager)
            .asEmptyDataResult()

    override suspend fun selectForAdventure(
        lobbyCode: String,
        players: List<String>,
    ): EmptyResult<DataError.Network> =
        GameControllerApi.selectForAdventure(lobbyCode = lobbyCode, body = players)
            .toResult(sessionManager)
            .asEmptyDataResult()

    override suspend fun guessMerlin(
        lobbyCode: String,
        username: String,
    ): EmptyResult<DataError.Network> =
        GameControllerApi.assassin(lobbyCode = lobbyCode, body = AssassinGuess(guess = username))
            .toResult(sessionManager)
            .asEmptyDataResult()

    companion object {
        /** Matches the frozen 2022 app's cadence; the server has no rate limit either way. */
        val DEFAULT_POLL_INTERVAL: Duration = 2.seconds
    }
}
