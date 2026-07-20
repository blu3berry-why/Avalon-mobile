/**
 * DTO <-> domain enum pairs.
 *
 * kmpgen inlines each response enum as a nested type on its owning DTO (`Info.Winner`,
 * `CharacterInfo.Name`) instead of hoisting a shared model, so the domain keeps its own
 * top-level enums and these @MapEnum pairs bridge by entry name. Entry names are identical
 * on both sides by construction — if the server adds a role, KSP fails the build here.
 */

package hu.blu3berry.avalon.core.data.mapper

import com.blu3berry.kraft.config.MapEnum
import com.blu3berry.kraft.config.MapReverse
import hu.blu3berry.avalon.core.data.generated.game.models.CharacterInfo
import hu.blu3berry.avalon.core.data.generated.game.models.Info
import hu.blu3berry.avalon.core.domain.model.Role
import hu.blu3berry.avalon.core.domain.model.Score
import hu.blu3berry.avalon.core.domain.model.Winner

@MapReverse
@MapEnum(source = Info.Winner::class, target = Winner::class)
object WinnerMapper

@MapReverse
@MapEnum(source = Info.ScoresItem::class, target = Score::class)
object ScoreMapper

@MapReverse
@MapEnum(source = CharacterInfo.Name::class, target = Role::class)
object RoleMapper
