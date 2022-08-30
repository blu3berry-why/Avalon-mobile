package hu.blu3berry.avalon.view.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.adapter.SelectRecycleViewAdapter
import hu.blu3berry.avalon.databinding.GameSelectBinding
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class SelectFragment : Fragment() {
    private lateinit var binding: GameSelectBinding

    private val viewModel:SelectViewModel by viewModels()

    @Inject
    lateinit var userUtils:UserUtils

    var count:Int = 1

    val adapter = SelectRecycleViewAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameSelectBinding.inflate(inflater, container, false)
        binding.lAssassinList.playersList.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)

        binding.btnChoose.setOnClickListener {
            if(adapter.selected.size != count){
                binding.tvSelectXPlayers.text = getString(R.string.select_x_not_x,count,adapter.selected.size)
                return@setOnClickListener
            }
            viewModel.select(userUtils.lobbyCode!!, adapter.selected).observe(viewLifecycleOwner){
                when(it.status){
                    Status.SUCCESS -> {
                        navController.navigate(R.id.on_back_to_game_main)
                    }
                    Status.ERROR ->{
                        Log.d("choose", it.message ?: "error")
                    }
                }
            }

        }

        viewModel.getInfo(userUtils.lobbyCode!!).observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS->{
                    adapter.setItems(it.data!!.playersName)
                    count = it.data.playerSelectNum
                    binding.btnChoose.isEnabled = true
                    binding.tvSelectXPlayers.text = getString(R.string.select_x,count)
                }
            }
        }

    }
}