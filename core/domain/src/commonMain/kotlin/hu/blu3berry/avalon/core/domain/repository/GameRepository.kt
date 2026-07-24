package hu.blu3berry.avalon.core.domain.repository

import hu.blu3berry.avalon.core.domain.model.Character
import hu.blu3berry.avalon.core.domain.model.GameInfo
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * In-game actions plus the live game state.
 *
 * [observeGameInfo] is the D1 seam: it is a polling loop today (the server exposes no push
 * channel), but callers only ever see a `Flow`, so swapping in a WebSocket later is a
 * repository-internal change. Nothing else in the app polls.
 */
interface GameRepository {

    /**
     * Emits the game state until the collector stops. Failures are emitted as
     * `Result.Failure` rather than terminating the flow — a dropped poll must not kill the
     * screen's subscription.
     */
    fun observeGameInfo(lobbyCode: String): Flow<Result<GameInfo, DataError.Network>>

    suspend fun getGameInfo(lobbyCode: String): Result<GameInfo, DataError.Network>

    suspend fun getCharacter(lobbyCode: String): Result<Character, DataError.Network>

    /** Vote to approve or reject the king's proposed team. */
    suspend fun voteOnTeam(
        lobbyCode: String,
        username: String,
        approve: Boolean,
    ): EmptyResult<DataError.Network>

    /** Play a success/fail card on the adventure the caller was selected for. */
    suspend fun voteOnAdventure(
        lobbyCode: String,
        username: String,
        succeed: Boolean,
    ): EmptyResult<DataError.Network>

    /** King-only: propose the team for the current adventure. */
    suspend fun selectForAdventure(
        lobbyCode: String,
        players: List<String>,
    ): EmptyResult<DataError.Network>

    /** Assassin-only, after good wins three adventures: name the player believed to be Merlin. */
    suspend fun guessMerlin(lobbyCode: String, username: String): EmptyResult<DataError.Network>
}
