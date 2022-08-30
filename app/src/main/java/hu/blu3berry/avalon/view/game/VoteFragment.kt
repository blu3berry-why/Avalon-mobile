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
import hu.blu3berry.avalon.adapter.PlayersRecyclerViewAdapter
import hu.blu3berry.avalon.databinding.GameVoteBinding
import hu.blu3berry.avalon.model.network.SingleVote
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class VoteFragment : Fragment() {
    private lateinit var binding: GameVoteBinding
    private val viewModel: VoteViewModel by viewModels()
    @Inject
    lateinit var userUtils:UserUtils

    var adapter = PlayersRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameVoteBinding.inflate(inflater, container, false)
        binding.lAssassinList.playersList.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)

        viewModel.getInfo(userUtils.lobbyCode!!).observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    adapter.setItems(it.data!!.selectedForAdventure)
                    binding.textView2.text = getString(R.string.king_selected, it.data.king)
                }
            }
        }

        binding.btnVoteNo.setOnClickListener {
            viewModel.vote(userUtils.lobbyCode!!, SingleVote(
                username = userUtils.getUser()!!,
                uservote = false
            )).observe(viewLifecycleOwner){
                when(it.status){
                    Status.SUCCESS -> {
                        navController.navigate(R.id.on_back_to_game_main)
                    }
                }
            }

        }
        binding.btnVoteYes.setOnClickListener {
            viewModel.vote(userUtils.lobbyCode!!, SingleVote(
                username = userUtils.getUser()!!,
                uservote = true
            )).observe(viewLifecycleOwner){
                when(it.status){
                    Status.SUCCESS -> {
                        navController.navigate(R.id.on_back_to_game_main)
                    }
                }
            }
        }
    }
}