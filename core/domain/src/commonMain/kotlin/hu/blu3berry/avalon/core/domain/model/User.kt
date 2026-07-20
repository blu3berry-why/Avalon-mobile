package hu.blu3berry.avalon.core.domain.model

/**
 * An account. The server's `LoginInfo` DTO doubles as its user representation and carries a
 * `password` field; the domain model deliberately drops it — nothing in the app displays or
 * re-sends a stored password, so the DTO -> domain mapper is one-way.
 *
 * `LoginInfo` declares every field nullable (it is reused as a request body), hence the
 * coalescing in the mapper: a user without a username cannot exist server-side.
 */
data class User(
    val username: String,
    val email: String?,
    val friends: List<User>,
)
