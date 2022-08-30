package hu.blu3berry.avalon.model.network




data class LoginInfo(
    val username: String,
    val password: String?,
    val email: String? = null,
    var friends: MutableList<LoginInfo>? = null,
)
