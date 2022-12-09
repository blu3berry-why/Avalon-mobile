package hu.blu3berry.avalon.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.StartPageBinding
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class StartPageFragment : Fragment() {
    private lateinit var binding: StartPageBinding

    @Inject
    lateinit var userUtils: UserUtils


    private val viewModel: StartPageViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = StartPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        binding.btnCreateLobby.setOnClickListener {
            viewModel.createLobby().observe(viewLifecycleOwner) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        val lc = response.data!!.code
                        viewModel.joinLobby(lc).observe(viewLifecycleOwner) {
                            when (it.status) {
                                Status.SUCCESS -> {
                                    userUtils.lobbyCode = lc
                                    navController.navigate(R.id.on_join_lobby)
                                }
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.etLobbyCode.error = getString(R.string.err_join)
                    }
                }
            }

        }

        binding.btnJoinLobby.setOnClickListener {
            if (isValid().not()) return@setOnClickListener
                viewModel.joinLobby(lobbyCode).observe(viewLifecycleOwner) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            userUtils.lobbyCode = lobbyCode
                                navController.navigate(R.id.on_join_lobby)
                        }

                        Status.ERROR -> {
                            binding.etLobbyCode.error = getString(R.string.err_not_found)
                        }
                    }
                }
        }
    }

    fun isValid(): Boolean {
        if (binding.etLobbyCode.text.isBlank()) {
            binding.etLobbyCode.error = getString(R.string.err_empty)
            return false
        }
        return true
    }

    val lobbyCode : String
    get() = binding.etLobbyCode.text.toString().lowercase()



}