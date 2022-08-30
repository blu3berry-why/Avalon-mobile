package hu.blu3berry.avalon.view.game

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.model.network.LobbyCode
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class GameMainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    fun getInfo(lobbyCode: String) = repository.gameInfo(lobbyCode)
}
