package hu.blu3berry.avalon.view

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.model.network.LoginInfo
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class StartPageViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    fun createLobby() = repository.createLobby()

    fun joinLobby(lobbyCode: String) = repository.joinLobby(lobbyCode)

}