package hu.blu3berry.avalon.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.AuthRegisterBinding
import hu.blu3berry.avalon.model.network.LoginInfo
import hu.blu3berry.avalon.network.TokenInterceptor
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment: Fragment() {

    private lateinit var binding: AuthRegisterBinding

    @Inject
    lateinit var userUtils: UserUtils

    @Inject
    lateinit var tokenInterceptor: TokenInterceptor

    var register = false

    private val viewModel:RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AuthRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        binding.btnRegisterUser.setOnClickListener {
            if (isValid().not()) return@setOnClickListener

            if(register) return@setOnClickListener

            register = true
            binding.pbRegister.visibility = View.VISIBLE

            viewModel.register(userData()).observe(viewLifecycleOwner){
                when(it.status){
                    Status.SUCCESS -> {
                        viewModel.login(userData()).observe(viewLifecycleOwner){
                        when(it.status){
                            Status.SUCCESS -> {
                                userUtils.token = it.data!!
                                tokenInterceptor.token = userUtils.getToken()!!
                                register = false
                                navController.navigate(R.id.on_register_action)
                            }
                            Status.LOADING ->{
                                binding.pbRegister.visibility = View.VISIBLE
                            }
                            Status.ERROR -> {
                                binding.etUsrenameR.error = getString(R.string.something_went_wrong)
                                register = false
                                binding.pbRegister.visibility = View.GONE
                            }
                        }

                        }
                    }
                }
            }


        }
    }

    fun isValid(): Boolean{
        var valid = true
        if(binding.etUsrenameR.text.isBlank()){
            valid = false
            binding.etUsrenameR.error = getString(R.string.fill_out_field)
        }

        if(binding.etPasswordR.text.isBlank()){
            valid = false
            binding.etPasswordR.error = getString(R.string.fill_out_field)
        }

        if(binding.etPasswordR.text.toString() != binding.etPasswordCheckR.text.toString()){
            valid = false
            binding.etPasswordCheckR.error = getString(R.string.match_pass)
        }

        if(binding.etEmailR.text.isBlank()){
            valid = false
            binding.etPasswordR.error = getString(R.string.fill_out_field)
        }
        return valid
    }

    fun userData() : LoginInfo{
        return LoginInfo(
            username = binding.etUsrenameR.text.toString(),
            password = binding.etPasswordR.text.toString(),
            email = binding.etEmailR.text.toString()
        )

    }
}