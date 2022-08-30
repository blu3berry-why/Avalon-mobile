package hu.blu3berry.avalon.view.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.blu3berry.avalon.model.network.LoginInfo
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    fun login(user: LoginInfo) = repository.loginUser(user)

}