package hu.blu3berry.avalon.core.domain.model

/**
 * A player's secret role. Entry names mirror the server's `ROLE` enum (and therefore the
 * generated `CharacterInfo.Name` DTO enum) so Kraft's @MapEnum pair maps by entry name.
 *
 * [isEvil] is carried client-side rather than fetched: the server's ROLE enum defines the
 * same split (`blu3berry/why/avalon/model/enums/ROLE.kt`) but never exposes it over the wire.
 */
enum class Role(val isEvil: Boolean) {
    PERCIVAL(isEvil = false),
    MERLIN(isEvil = false),
    SERVANT_OF_ARTHUR(isEvil = false),
    ARNOLD(isEvil = false),
    ASSASSIN(isEvil = true),
    MORGANA(isEvil = true),
    MORDRED(isEvil = true),
    OBERON(isEvil = true),
    MINION_OF_MORDRED(isEvil = true),
}
