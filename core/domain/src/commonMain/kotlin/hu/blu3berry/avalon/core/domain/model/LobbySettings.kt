package hu.blu3berry.avalon.core.domain.model

/** Which optional roles are in play for a lobby. Mirrors the server's `Settings` DTO 1:1. */
data class LobbySettings(
    val assassin: Boolean,
    val mordred: Boolean,
    val morgana: Boolean,
    val oberon: Boolean,
    val percival: Boolean,
    val arnold: Boolean,
)
