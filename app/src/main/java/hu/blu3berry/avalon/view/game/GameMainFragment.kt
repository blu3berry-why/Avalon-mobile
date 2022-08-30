package hu.blu3berry.avalon.view.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.model.enums.SCORE
import com.example.model.enums.WINNER
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.GameMainBinding
import hu.blu3berry.avalon.model.network.Info
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class GameMainFragment : Fragment() {
    private lateinit var binding: GameMainBinding

    @Inject
    lateinit var userUtils: UserUtils

    private lateinit var timer: Timer

    private val viewModel: GameMainViewModel by viewModels()

    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameMainBinding.inflate(inflater, container, false)
        setVisibility(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)


        updateGameInfo()
        binding.BtnCharacterMain.setOnClickListener {
            navController.navigate(R.id.on_character_action)
        }

        binding.btnAdventure.setOnClickListener {
            navController.navigate(R.id.on_adventure_vote_action)
        }

        binding.btnSelectMain.setOnClickListener {
            navController.navigate(R.id.on_game_select_action)
        }

        binding.btnVoteMain.setOnClickListener {
            navController.navigate(R.id.on_vote_action)
        }

    }

    override fun onResume() {
        super.onResume()

        var timerTask: TimerTask = timerTask {
            updateGameInfo()
        }
        try {
            timer = Timer()
            timer.schedule(timerTask, 2000, 2000)
        } catch (e: IllegalStateException) {
            Log.w("timer", e)
        }
    }

    private fun updateGameInfo() {
        activity?.runOnUiThread {
            viewModel.getInfo(userUtils.lobbyCode!!).observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        setUI(it.data!!)
                        setVisibility(true)
                    }
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }

    private fun setUI(info: Info) {
        when (info.winner) {
            WINNER.NOT_DECIDED -> {}
            WINNER.EVIL -> navController.navigate(R.id.on_game_evil_win)
            WINNER.GOOD -> navController.navigate(R.id.on_game_good_win)
        }

        binding.btnVoteMain.isEnabled = !info.isAdventure
        if (info.selectedForAdventure.isEmpty()) {
            binding.btnVoteMain.isEnabled = false
        }

        binding.btnAdventure.isEnabled =
            info.selectedForAdventure.contains(userUtils.getUser()) && info.isAdventure

        binding.btnSelectMain.visibility = View.GONE
        if (info.king == userUtils.getUser()) {
            binding.btnSelectMain.visibility = View.VISIBLE
        }
        val list = listOf(
            binding.ivFirst,
            binding.ivSecond,
            binding.ivThird,
            binding.ivFourth,
            binding.ivFifth
        )
        for (i in 0 until 5) {
            when (info.scores[i]) {
                SCORE.EVIL -> {
                    list[i].setImageResource(R.drawable.evil_c)
                }
                SCORE.GOOD -> {
                    list[i].setImageResource(R.drawable.good_c)
                }
                SCORE.UNDECIDED -> {
                    list[i].setImageResource(R.drawable.neutral_c)
                }
            }
        }

    }

    fun setVisibility(visible:Boolean){
        val v = if(visible) View.VISIBLE else View.INVISIBLE

        binding.ivFirst.visibility = v
        binding.ivSecond.visibility = v
        binding.ivThird.visibility = v
        binding.ivFourth.visibility = v
        binding.ivFifth.visibility = v

    }
}