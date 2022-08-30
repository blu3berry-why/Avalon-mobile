package hu.blu3berry.avalon.view.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.GameVoteOnAdventureBinding
import hu.blu3berry.avalon.model.network.SingleVote
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class VoteOnAdventureFragment : Fragment() {
    private lateinit var binding: GameVoteOnAdventureBinding

    private val viewModel: VoteOnAdventureViewModel by viewModels()

    @Inject
    lateinit var userUtils: UserUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameVoteOnAdventureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        binding.btnAdventureNo.setOnClickListener {
            viewModel.vote(
                userUtils.lobbyCode!!, SingleVote(
                    username = userUtils.getUser()!!,
                    uservote = false
                )
            ).observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        navController.navigate(R.id.on_back_to_game_main)
                    }
                }
            }
        }
        binding.btnAdventureYes.setOnClickListener {
            viewModel.vote(
                userUtils.lobbyCode!!, SingleVote(
                    username = userUtils.getUser()!!,
                    uservote = true
                )
            ).observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        navController.navigate(R.id.on_back_to_game_main)
                    }
                }
            }
        }
    }
}