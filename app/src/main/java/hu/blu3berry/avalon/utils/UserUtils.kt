package hu.blu3berry.avalon.utils

import hu.blu3berry.avalon.model.network.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUtils @Inject constructor() {
    var token: Token? = null
    var lobbyCode: String? = null
    fun getUser() = token?.username
    fun getToken() = token?.token

}