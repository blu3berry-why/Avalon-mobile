package hu.blu3berry.avalon.view.auth

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.AuthLoginBinding
import hu.blu3berry.avalon.model.network.LoginInfo
import hu.blu3berry.avalon.network.TokenInterceptor
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: AuthLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var userUtils: UserUtils

    @Inject
    lateinit var tokenInterceptor: TokenInterceptor

    @Inject
    lateinit var pref: SharedPreferences

    lateinit var navController: NavController

    var loggingIn = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AuthLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        val USERNAME = "USERNAME"
        val PASSWORD = "PASSWORD"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LoginInfo(
            pref.getString(USERNAME, "") ?: "",
            password = pref.getString(PASSWORD, "")
        ).let {
            if (it.username != "") {
                login(it)
            }
        }



        navController = Navigation.findNavController(view)

        binding.btnLogin.setOnClickListener {
            if (isValid().not()) {
                return@setOnClickListener
            }
            getUser().let {
                login(it)
                with(pref.edit()) {
                    putString(USERNAME, it.username)
                    putString(PASSWORD, it.password)
                    apply()
                }
            }

        }

        binding.btnRegister.setOnClickListener {
            navController.navigate(R.id.on_go_register_action)
        }
    }

    fun isValid(): Boolean {
        var valid = true
        if (binding.etUsername.text.isBlank()) {
            valid = false
            binding.etUsername.error = getString(R.string.fill_out_field)
        }

        if (binding.etPassword.text.isBlank()) {
            valid = false
            binding.etPassword.error = getString(R.string.fill_out_field)
        }
        return valid
    }

    fun getUser(): LoginInfo {
        return LoginInfo(
            username = binding.etUsername.text.toString(),
            password = binding.etPassword.text.toString(),
        )
    }

    fun login(user: LoginInfo) {
        binding.pbLogin.visibility = View.VISIBLE

        if (loggingIn) return
        loggingIn = true

        viewModel.login(user).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    loggingIn = false
                    userUtils.token = it.data!!
                    tokenInterceptor.token = userUtils.getToken()!!
                    Log.d("login", userUtils.getToken() ?: "")
                    navController.navigate(R.id.on_login_action)
                }

                Status.LOADING -> {
                    binding.pbLogin.visibility = View.VISIBLE
                }

                Status.ERROR -> {
                    loggingIn = false
                    binding.pbLogin.visibility = View.GONE
                    binding.etUsername.error = getString(R.string.invalid_pass)
                }
            }
        }
    }
}