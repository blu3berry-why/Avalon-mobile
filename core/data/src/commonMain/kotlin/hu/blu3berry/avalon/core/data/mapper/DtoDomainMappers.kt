/**
 * DTO <-> domain @MapConfig declarations. Nested enum fields resolve through the @MapEnum
 * pairs in `EnumMappers.kt`; the `LoginInfo` nullability coercions live in `Converters.kt`.
 *
 * Only `Settings` needs a reverse direction (PUT /lobby/{code}/settings takes the same shape
 * back). `Info` and `CharacterInfo` are server-produced only, and `User` deliberately drops
 * the DTO's `password`, so a generated reverse would be lossy — those stay forward-only.
 */

package hu.blu3berry.avalon.core.data.mapper

import com.blu3berry.kraft.config.FieldMapping
import com.blu3berry.kraft.config.MapConfig
import com.blu3berry.kraft.config.MapReverse
import com.blu3berry.kraft.config.MapUsing
import hu.blu3berry.avalon.core.data.generated.game.models.CharacterInfo
import hu.blu3berry.avalon.core.data.generated.game.models.Info
import hu.blu3berry.avalon.core.data.generated.game.models.LoginInfo
import hu.blu3berry.avalon.core.data.generated.game.models.Settings
// Kraft side aliases (`toDomain()`), configured in this module's build.gradle.kts.
import hu.blu3berry.avalon.core.data.generated.game.models.generated.*
import hu.blu3berry.avalon.core.domain.model.Character
import hu.blu3berry.avalon.core.domain.model.GameInfo
import hu.blu3berry.avalon.core.domain.model.LobbySettings
import hu.blu3berry.avalon.core.domain.model.User

@MapConfig(
    source = Info::class,
    target = GameInfo::class,
    fieldMappings = [FieldMapping(source = "playersName", target = "players")],
)
object GameInfoMapper

@MapConfig(
    source = CharacterInfo::class,
    target = Character::class,
    fieldMappings = [FieldMapping(source = "name", target = "role")],
)
object CharacterMapper

@MapReverse
@MapConfig(source = Settings::class, target = LobbySettings::class)
object LobbySettingsMapper

/**
 * `LoginInfo` is reused as a request body, so every field is nullable on the wire while the
 * domain [User] is not. Both coercions are per-property rather than @KraftConverter: Kraft
 * rejects converters with parameterized receivers, which rules out a `List<LoginInfo>?` one.
 */
@MapConfig(source = LoginInfo::class, target = User::class)
object UserMapper {

    /** A user without a username cannot exist server-side; an empty name is the inert fallback. */
    @MapUsing(source = "username", target = "username")
    fun coerceUsername(value: String?): String = value.orEmpty()

    /**
     * `friends` is absent (not `[]`) for users who have none. Recurses through this same
     * mapper, so a friend's own friends map too.
     */
    @MapUsing(source = "friends", target = "friends")
    fun coerceFriends(value: List<LoginInfo>?): List<User> = value.orEmpty().map { it.toDomain() }
}
