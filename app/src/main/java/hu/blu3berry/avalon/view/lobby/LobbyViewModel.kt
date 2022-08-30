package hu.blu3berry.avalon.view.lobby

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    fun startLobby(lobbyCode: String) = repository.startLobby(lobbyCode)

    fun getInfo(lobbyCode: String) = repository.gameInfo(lobbyCode)
}