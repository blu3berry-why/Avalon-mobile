package hu.blu3berry.avalon.view.game

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.model.network.AssassinGuess
import hu.blu3berry.avalon.model.network.LobbyCode
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class AssassinGuessViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    fun guess(lobbyCode: String, user: String) = repository.assassinGuess(lobbyCode, AssassinGuess(user))
    fun getInfo(lobbyCode: String) = repository.gameInfo(lobbyCode)
}