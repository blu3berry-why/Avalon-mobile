package hu.blu3berry.avalon.view.game

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class SelectViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    fun getInfo(lobbyCode:String) = repository.gameInfo(lobbyCode)
    fun select(lobbyCode: String, selected:List<String>) = repository.select(lobbyCode,selected)
}