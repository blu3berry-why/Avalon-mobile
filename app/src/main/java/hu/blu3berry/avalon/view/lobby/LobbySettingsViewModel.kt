package hu.blu3berry.avalon.view.lobby

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.model.network.Settings
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class LobbySettingsViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    fun getSettings(lobbyCode:String) = repository.getLobbySettings(lobbyCode)

    fun setSettings(lobbyCode: String, settings: Settings) = repository.setLobbySettings(lobbyCode, settings)
}