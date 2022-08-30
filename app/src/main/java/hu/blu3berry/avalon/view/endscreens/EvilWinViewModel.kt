package hu.blu3berry.avalon.view.endscreens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject


@HiltViewModel
class EvilWinViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

}