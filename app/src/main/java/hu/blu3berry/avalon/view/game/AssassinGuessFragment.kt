package hu.blu3berry.avalon.view.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.adapter.AssassinGuessRecycleViewAdapter
import hu.blu3berry.avalon.databinding.GameAssassinGuessBinding
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class AssassinGuessFragment: Fragment(), UserSelectedListener{

    private lateinit var binding: GameAssassinGuessBinding

    private val viewModel:AssassinGuessViewModel by viewModels()

    val adapter = AssassinGuessRecycleViewAdapter(this)

    lateinit var navController: NavController

    @Inject
    lateinit var userUtils: UserUtils


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameAssassinGuessBinding.inflate(inflater, container, false)
        binding.lAssassinList.playersList.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        viewModel.getInfo(userUtils.lobbyCode!!).observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    adapter.setItems(it.data!!.playersName)
                }
            }
        }

        binding.btnAssassinGuessBack.setOnClickListener {
            navController.navigate(R.id.on_back_to_character)
        }
    }

    override fun onUserSelected(user: String){
        viewModel.guess(userUtils.lobbyCode!!, user).observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    navController.navigate(R.id.on_back_to_character)
                }
            }
        }
    }


}
interface UserSelectedListener{
    fun onUserSelected(user: String)
}