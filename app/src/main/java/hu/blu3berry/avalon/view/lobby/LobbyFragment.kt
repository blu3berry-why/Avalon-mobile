package hu.blu3berry.avalon.view.lobby

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
import hu.blu3berry.avalon.adapter.PlayersRecyclerViewAdapter
import hu.blu3berry.avalon.databinding.LobbyMainBinding
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class LobbyFragment : Fragment() {
    private lateinit var binding: LobbyMainBinding
    private val viewModel: LobbyViewModel by viewModels()

    @Inject
    lateinit var userUtils: UserUtils

    private lateinit var adapter: PlayersRecyclerViewAdapter

    lateinit var navController: NavController

    private lateinit var timer: Timer


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = LobbyMainBinding.inflate(inflater, container, false)
        adapter = PlayersRecyclerViewAdapter()
        binding.lAssassinList.playersList.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        updatePlayers()

        binding.tvLobbyCode.text = userUtils.lobbyCode!!.uppercase()

        binding.btnStart.setOnClickListener {
            if(binding.lAssassinList.playersList.adapter!!.itemCount <5 ){
                binding.tvErrorLobby.text = getString(R.string.min_players)
                return@setOnClickListener
            }
            if( 10 < binding.lAssassinList.playersList.adapter!!.itemCount){
                binding.tvErrorLobby.text = getString(R.string.max_players)
                return@setOnClickListener
            }

            viewModel.startLobby(userUtils.lobbyCode!!).observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        navController.navigate(R.id.on_lobby_started_action)
                    }
                }
            }

        }

        binding.btnSettings.setOnClickListener {
            navController.navigate(R.id.on_settings_action)
        }

    }

    override fun onResume() {
        super.onResume()

        var timerTask: TimerTask = timerTask {
            updatePlayers()
        }
        try {
            timer = Timer()
            timer.schedule(timerTask, 2000, 2000)
        } catch (e: IllegalStateException) {
            Log.w("timer", e)
        }
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }


    private fun updatePlayers() {
        activity?.runOnUiThread {
            viewModel.getInfo(userUtils.lobbyCode!!).observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        adapter.setItems(it.data!!.playersName)
                        if (it.data.started) {
                            navController.navigate(R.id.on_lobby_started_action)
                        }
                    }
                }
            }
        }
    }
}