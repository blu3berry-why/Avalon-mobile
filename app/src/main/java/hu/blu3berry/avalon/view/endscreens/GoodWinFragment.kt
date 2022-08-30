package hu.blu3berry.avalon.view.endscreens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.model.enums.ROLE
import com.example.model.enums.WINNER
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.GameGoodWinBinding
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask


@AndroidEntryPoint
class GoodWinFragment : Fragment() {
    private lateinit var binding: GameGoodWinBinding

    private lateinit var timer: Timer

    @Inject
    lateinit var userUtils:UserUtils

    private val viewModel: GoodWinViewModel by viewModels()

    private lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameGoodWinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.btnAssassinGuessG.setOnClickListener {
            navController.navigate(R.id.on_assassin_guess_action)
        }

        viewModel.getCharacter(userUtils.lobbyCode!!).observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS ->{
                    if (it.data!!.name == ROLE.ASSASSIN){
                        binding.btnAssassinGuessG.isEnabled = true
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        var timerTask: TimerTask = timerTask {
            update()
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

    fun update(){
        activity?.runOnUiThread {
            viewModel.getGameInfo(userUtils.lobbyCode!!).observe(viewLifecycleOwner){ it ->
                when(it.status){
                    Status.SUCCESS -> {
                        if(it.data!!.assassinHasGuessed){
                            when(it.data.winner){
                                WINNER.NOT_DECIDED -> {}

                                WINNER.EVIL -> {
                                    binding.btnAssassinGuessG.isEnabled = false
                                    binding.btnBackToMainG.isEnabled = true
                                    navController.navigate(R.id.on_game_evil_win)
                                }
                                WINNER.GOOD -> {
                                    viewModel.getSettings(userUtils.lobbyCode!!).observe(viewLifecycleOwner){ settings->
                                        when(settings.status){
                                            Status.SUCCESS -> {
                                                if(!settings.data!!.assassin){
                                                    binding.btnAssassinGuessG.visibility = View.GONE
                                                    binding.btnBackToMainG.isEnabled = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}