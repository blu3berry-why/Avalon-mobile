package hu.blu3berry.avalon.core.data.mapper

import hu.blu3berry.avalon.core.data.generated.game.models.LoginInfo
import hu.blu3berry.avalon.core.data.generated.game.models.generated.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Kraft generates and type-checks the mappings themselves — a wrong `@FieldMapping` name or a
 * mismatched enum entry fails at KSP time, so field-for-field mapper tests would only restate
 * what the processor already proves.
 *
 * What KSP cannot check is the hand-written `@MapUsing` bodies in `UserMapper`: `LoginInfo` is
 * reused as a request body and declares every field nullable, and the coercions onto the
 * non-null domain `User` are ordinary Kotlin that can be wrong. Those are the only mappings
 * tested here.
 */
class MapperTest {

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
}
