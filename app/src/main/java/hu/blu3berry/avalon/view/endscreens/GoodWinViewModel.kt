package hu.blu3berry.avalon.view.endscreens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.model.network.LobbyCode
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class GoodWinViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    fun getCharacter(lobbyCode: String) = repository.characterInfo(lobbyCode)
    fun getGameInfo(lobbyCode: String) = repository.gameInfo(lobbyCode)
    fun getSettings(lobbyCode: String) = repository.getLobbySettings(lobbyCode)
}