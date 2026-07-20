package hu.blu3berry.avalon.core.data.mapper

import hu.blu3berry.avalon.core.data.generated.game.models.CharacterInfo
import hu.blu3berry.avalon.core.data.generated.game.models.Info
import hu.blu3berry.avalon.core.data.generated.game.models.LoginInfo
import hu.blu3berry.avalon.core.data.generated.game.models.generated.*
import hu.blu3berry.avalon.core.domain.model.LobbySettings
import hu.blu3berry.avalon.core.domain.model.Role
import hu.blu3berry.avalon.core.domain.model.Score
import hu.blu3berry.avalon.core.domain.model.Winner
import hu.blu3berry.avalon.core.domain.model.generated.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the parts of the Kraft mapping that are not name-for-name copies: the renamed
 * fields, the nested DTO enums, and the `LoginInfo` nullability coercions.
 */
class MapperTest {

    @Test
    fun `Info maps playersName onto players and bridges both nested enums`() {
        val domain = Info(
            started = true,
            winner = Info.Winner.GOOD,
            scores = listOf(Info.ScoresItem.GOOD, Info.ScoresItem.EVIL, Info.ScoresItem.UNDECIDED),
            currentRound = 2,
            isAdventure = false,
            currentAdventure = 3,
            king = "arthur",
            failCounter = 1,
            selectedForAdventure = listOf("arthur", "merlin"),
            playersName = listOf("arthur", "merlin", "mordred"),
            assassinHasGuessed = false,
            playerSelectNum = 2,
        ).toDomain()

        assertEquals(listOf("arthur", "merlin", "mordred"), domain.players)
        assertEquals(Winner.GOOD, domain.winner)
        assertEquals(listOf(Score.GOOD, Score.EVIL, Score.UNDECIDED), domain.scores)
        assertEquals("arthur", domain.king)
    }

    @Test
    fun `CharacterInfo name maps onto role and carries the evil flag`() {
        val domain = CharacterInfo(
            name = CharacterInfo.Name.MORGANA,
            sees = listOf("mordred"),
        ).toDomain()

        assertEquals(Role.MORGANA, domain.role)
        assertTrue(domain.role.isEvil)
        assertEquals(listOf("mordred"), domain.sees)
    }

    @Test
    fun `LoginInfo with every optional field absent still yields a usable User`() {
        val domain = LoginInfo().toDomain()

        assertEquals("", domain.username)
        assertEquals(null, domain.email)
        assertEquals(emptyList(), domain.friends)
    }

    @Test
    fun `LoginInfo friends map recursively`() {
        val domain = LoginInfo(
            username = "arthur",
            password = "hunter2",
            email = "arthur@camelot.example",
            friends = listOf(LoginInfo(username = "merlin", friends = listOf(LoginInfo(username = "percival")))),
        ).toDomain()

        assertEquals("arthur", domain.username)
        assertEquals("arthur@camelot.example", domain.email)
        assertEquals(listOf("merlin"), domain.friends.map { it.username })
        assertEquals(listOf("percival"), domain.friends.single().friends.map { it.username })
    }

    @Test
    fun `LobbySettings round-trips through the DTO`() {
        val settings = LobbySettings(
            assassin = true,
            mordred = false,
            morgana = true,
            oberon = false,
            percival = true,
            arnold = false,
        )

        assertEquals(settings, settings.toDto().toDomain())
    }
}
