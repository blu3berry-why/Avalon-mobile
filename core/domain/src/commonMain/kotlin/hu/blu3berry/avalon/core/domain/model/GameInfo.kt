package hu.blu3berry.avalon.core.domain.model

/**
 * Public game state — everything every player may see. Polled today, pushed over a socket
 * later (D1); the repository owns that seam, this model is unaffected either way.
 *
 * Field names track the server's `Info` DTO so the Kraft mapper needs no field mappings
 * beyond `playersName` -> [players].
 */
data class GameInfo(
    val started: Boolean,
    val winner: Winner,
    val scores: List<Score>,
    val currentRound: Int,
    val isAdventure: Boolean,
    val currentAdventure: Int,
    val king: String?,
    val failCounter: Int,
    val selectedForAdventure: List<String>,
    val players: List<String>,
    val assassinHasGuessed: Boolean,
    val playerSelectNum: Int,
)

/**
 * The caller's own secret info: their [role] and the players their role lets them see
 * (Merlin sees evil, Percival sees Merlin+Morgana, evil sees evil, ...). Server-side
 * `CharacterInfo` calls the role field `name`.
 */
data class Character(
    val role: Role,
    val sees: List<String>,
)
