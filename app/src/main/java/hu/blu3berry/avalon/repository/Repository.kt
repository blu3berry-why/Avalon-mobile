package hu.blu3berry.avalon.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import hu.blu3berry.avalon.model.network.*
import hu.blu3berry.avalon.network.RestAPI
import hu.blu3berry.avalon.utils.Resource
import hu.blu3berry.avalon.utils.performGetOperation
import hu.blu3berry.avalon.utils.performPostOperation
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class Repository @Inject constructor(
    private val restapi: RestAPI) {


    //--------------------------------------
    //      AUTHENTICATION
    //--------------------------------------

    fun loginUser(user: LoginInfo): LiveData<Resource<Token>> = liveData(Dispatchers.IO) {
        val responseStatus = restapi.loginUser(user)
        emit(responseStatus)
    }

    fun register(user:LoginInfo) = performPostOperation {
        restapi.register(user)
    }

    //------------------------------
    //   USER
    //------------------------------

    fun getUser(userName: String) = performGetOperation { restapi.getUser(userName) }

    fun editUser(user: LoginInfo) = performPostOperation { restapi.editUser(user) }

    fun deleteUser(userName: String) = performPostOperation { restapi.deleteUser(userName) }

    //------------------------------
    //   LOBBY
    //------------------------------

    fun getLobbySettings(lobbyCode: String) = performGetOperation { restapi.getLobbySettings(lobbyCode) }

    fun setLobbySettings(lobbyCode: String, settings: Settings) =
        performPostOperation { restapi.setLobbySettings(lobbyCode, settings) }

    fun createLobby() =
        performGetOperation { restapi.createLobby() }

    fun joinLobby(lobbyCode: String) =
        performPostOperation { restapi.joinLobby(lobbyCode) }

    fun leaveLobby(lobbyCode: String) =
        performPostOperation { restapi.leaveLobby(lobbyCode) }

    fun startLobby(lobbyCode: String) =
        performPostOperation { restapi.startLobby(lobbyCode) }
//------------------------------
    //   GAME
    //------------------------------

    fun gameInfo(lobbyCode: String) =
        performGetOperation { restapi.gameInfo(lobbyCode) }

    fun characterInfo(lobbyCode: String) =
        performGetOperation { restapi.characterInfo(lobbyCode) }

    fun select(lobbyCode: String, chosen: List<String>) =
        performPostOperation { restapi.select(lobbyCode,chosen) }

    fun vote(lobbyCode: String, vote: SingleVote) =
        performPostOperation { restapi.vote(lobbyCode, vote) }

    fun voteOnAdventure(lobbyCode: String, vote: SingleVote) =
        performPostOperation { restapi.voteOnAdventure(lobbyCode, vote) }

    fun assassinGuess(lobbyCode: String, assassinGuess: AssassinGuess) =
        performPostOperation { restapi.assassinGuess(lobbyCode, assassinGuess) }


}