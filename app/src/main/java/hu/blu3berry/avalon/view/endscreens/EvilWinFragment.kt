package hu.blu3berry.avalon.view.endscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.GameEvilWinBinding
import hu.blu3berry.avalon.databinding.GameGoodWinBinding
import hu.blu3berry.avalon.view.game.GameMainViewModel


@AndroidEntryPoint
class EvilWinFragment : Fragment() {
    private lateinit var binding: GameEvilWinBinding

    private val viewModel: EvilWinViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameEvilWinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)

        binding.btnBackToMainE.setOnClickListener {
            navController.navigate(R.id.on_back_to_main_screen)
        }

    }
}