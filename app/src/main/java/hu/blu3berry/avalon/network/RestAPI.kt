package hu.blu3berry.avalon.network

import android.util.Log
import hu.blu3berry.avalon.model.network.*
import hu.blu3berry.avalon.utils.Resource
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestAPI @Inject constructor (
    private val restApiInterface: RestApiInterface
        ) {

    companion object{
        const val BASE_URL = "https://ktor-avalon-rest.herokuapp.com/"
    }

    //--------------------------------------
    //      AUTHENTICATION
    //--------------------------------------

    suspend fun loginUser(user: LoginInfo): Resource<Token> {
        try {
            val response = restApiInterface.loginUser(user)
            if (response.isSuccessful) {
                Log.d("debug", response.body().toString())
                val body = response.body()
                if (body != null) return Resource.success(body)
            }
            return error(" ${response.code()} ${response.message()}")
        } catch (e: Exception) {
            return error(e.message ?: e.toString())
        }
    }

    suspend fun register(user:LoginInfo) = getResult { restApiInterface.register(user) }

    //------------------------------
    //   USER
    //------------------------------

    suspend fun getUser(userName: String) = getResult { restApiInterface.getUser(userName) }

    suspend fun editUser(user: LoginInfo) = getResult { restApiInterface.editUser(user) }

    suspend fun deleteUser(userName: String) = getResult { restApiInterface.deleteUser(userName) }

    //------------------------------
    //   LOBBY
    //------------------------------

    suspend fun getLobbySettings(lobbyCode: String) = getResult { restApiInterface.getLobbySettings(lobbyCode) }

    suspend fun setLobbySettings(lobbyCode: String, settings: Settings) = getResult { restApiInterface.setLobbySettings(lobbyCode, settings) }

    suspend fun createLobby() = getResult { restApiInterface.createLobby() }

    suspend fun joinLobby(lobbyCode: String) = getResult { restApiInterface.joinLobby(lobbyCode) }

    suspend fun leaveLobby(lobbyCode: String) = getResult { restApiInterface.leaveLobby(lobbyCode) }

    suspend fun startLobby(lobbyCode: String) = getResult { restApiInterface.startLobby(lobbyCode) }

    //------------------------------
    //   GAME
    //------------------------------

    suspend fun gameInfo(lobbyCode: String) = getResult { restApiInterface.gameInfo(lobbyCode) }

    suspend fun characterInfo(lobbyCode: String) = getResult { restApiInterface.characterInfo(lobbyCode) }

    suspend fun select(lobbyCode: String, chosen: List<String>) = getResult { restApiInterface.select(lobbyCode, chosen) }

    suspend fun vote(lobbyCode: String, vote: SingleVote) = getResult { restApiInterface.vote(lobbyCode, vote) }

    suspend fun voteOnAdventure(lobbyCode: String, vote: SingleVote) = getResult { restApiInterface.voteOnAdventure(lobbyCode, vote) }

    suspend fun assassinGuess(lobbyCode: String, assassinGuess: AssassinGuess) = getResult { restApiInterface.assassinGuess(lobbyCode, assassinGuess) }

    private suspend fun <T> getResult(call: suspend () -> Response<T>): Resource<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Resource.success(body)
            }
            return error(" ${response.code()} ${response.message()}")
        } catch (e: Exception) {
            return error(e.message ?: e.toString())
        }
    }

    private fun <T> error(message: String): Resource<T> {
        Log.d("error", message)
        return Resource.error("Network call has failed for a following reason: $message")
    }
}