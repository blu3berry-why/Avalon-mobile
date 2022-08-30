package hu.blu3berry.avalon.network

import hu.blu3berry.avalon.model.network.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface RestApiInterface {

    //------------------------------
    //   AUTHENTICATION
    //------------------------------

    @POST("login")
    suspend fun loginUser(
        @Body user: LoginInfo
    ):Response<Token>

    @POST("register")
    suspend fun register(
        @Body user: LoginInfo
    ):Response<ResponseBody>

    //------------------------------
    //   USER
    //------------------------------

    @GET("user/{username}")
    suspend fun getUser(
        @Path("username") userName: String
    ): Response<LoginInfo>

    @PUT("user")
    suspend fun editUser(
        @Body user: LoginInfo
    ):Response<ResponseBody>

    @DELETE("user/{username}")
    suspend fun deleteUser(
        @Path("username") userName: String
    ): Response<ResponseBody>

    //------------------------------
    //   LOBBY
    //------------------------------

    @GET("settings/{lobbyid}")
    suspend fun getLobbySettings(
        @Path("lobbyid") lobbyCode: String
    ): Response<Settings>

    @PUT("settings/{lobbyid}")
    suspend fun setLobbySettings(
        @Path("lobbyid") lobbyCode: String,
        @Body settings: Settings
    ): Response<ResponseBody>

    @POST("createlobby")
    suspend fun createLobby(): Response<LobbyCode>

    @POST("join/{lobbyid}")
    suspend fun joinLobby(
        @Path("lobbyid") lobbyCode: String
    ): Response<ResponseBody>

    @POST("leave/{lobbyid}")
    suspend fun leaveLobby(
        @Path("lobbyid") lobbyCode: String
    ): Response<ResponseBody>

    @POST("start/{lobbyid}")
    suspend fun startLobby(
        @Path("lobbyid") lobbyCode: String
    ): Response<ResponseBody>


    //------------------------------
    //   GAME
    //------------------------------

    @GET("game/{lobbyid}")
    suspend fun gameInfo(
        @Path("lobbyid") lobbyCode: String
    ): Response<Info>

    @GET("game/{lobbyid}/character")
    suspend fun characterInfo(
        @Path("lobbyid") lobbyCode: String
    ): Response<CharacterInfo>

    @POST("game/{lobbyid}/select")
    suspend fun select(
        @Path("lobbyid") lobbyCode: String,
        @Body chosen : List<String>
    ): Response<ResponseBody>


    @POST("game/{lobbyid}/vote")
    suspend fun vote(
        @Path("lobbyid") lobbyCode: String,
        @Body vote : SingleVote
    ): Response<ResponseBody>

    @POST("game/{lobbyid}/adventurevote")
    suspend fun voteOnAdventure(
        @Path("lobbyid") lobbyCode: String,
        @Body vote : SingleVote
    ): Response<ResponseBody>

    @POST("game/{lobbyid}/assassin")
    suspend fun assassinGuess(
        @Path("lobbyid") lobbyCode: String,
        @Body vote : AssassinGuess
    ): Response<ResponseBody>



}