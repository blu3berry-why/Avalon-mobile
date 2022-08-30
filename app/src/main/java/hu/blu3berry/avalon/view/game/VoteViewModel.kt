package hu.blu3berry.avalon.view.game

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.model.network.SingleVote
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    fun vote(lobbyCode: String,vote:SingleVote) = repository.vote(lobbyCode,vote)
    fun getInfo(lobbyCode: String) = repository.gameInfo(lobbyCode)
}
