package hu.blu3berry.avalon.view.game

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    fun getCharInfo(lobbyCode: String) = repository.characterInfo(lobbyCode)
}