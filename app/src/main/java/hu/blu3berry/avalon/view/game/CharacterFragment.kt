package hu.blu3berry.avalon.view.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.model.enums.ROLE
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.adapter.PlayersRecyclerViewAdapter
import hu.blu3berry.avalon.databinding.GameCharacterBinding
import hu.blu3berry.avalon.model.network.CharacterInfo
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class CharacterFragment : Fragment() {
    private lateinit var binding: GameCharacterBinding

    private val viewModel: CharacterViewModel by viewModels()
    private lateinit var adapter: PlayersRecyclerViewAdapter


    @Inject
    lateinit var userUtils: UserUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = GameCharacterBinding.inflate(inflater, container, false)
        adapter = PlayersRecyclerViewAdapter()
        setVisibility(false)
        binding.lAssassinList.playersList.adapter = adapter
        getData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)



        binding.btnAssassinGuess.setOnClickListener {

            navController.navigate(R.id.on_assassin_guess_action)
        }

        binding.btnBackToMain.setOnClickListener {
            navController.navigate(R.id.on_back_to_game_main)
        }


    }

    private fun getData() {
        viewModel.getCharInfo(userUtils.lobbyCode!!).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    setUI(it.data!!)
                    setVisibility(true)
                }
            }
        }
    }

    private fun setUI(info: CharacterInfo) {
        if(info.name == ROLE.ASSASSIN) {
            binding.btnAssassinGuess.visibility = View.VISIBLE
        }
        binding.tvCharacterName.text = when(info.name){
            ROLE.PERCIVAL -> getString(R.string.percival)
            ROLE.MERLIN -> getString(R.string.merlin)
            ROLE.SERVANT_OF_ARTHUR -> getString(R.string.servant)
            ROLE.ARNOLD -> getString(R.string.arnold)
            ROLE.ASSASSIN -> getString(R.string.assassin)
            ROLE.MORGANA -> getString(R.string.morgana)
            ROLE.MORDRED -> getString(R.string.mordred)
            ROLE.OBERON -> getString(R.string.oberon)
            ROLE.MINION_OF_MORDRED -> getString(R.string.minion)
        }


        binding.ivCharacter.setImageResource(when(info.name){
            ROLE.PERCIVAL -> R.drawable.percival_placeholder_c
            ROLE.MERLIN -> R.drawable.merlin_c
            ROLE.SERVANT_OF_ARTHUR -> R.drawable.servant_of_arthur_c
            ROLE.ARNOLD -> R.drawable.arnold_placeholder_c
            ROLE.ASSASSIN -> R.drawable.assassin_placeholder_c
            ROLE.MORGANA -> R.drawable.morgana_c
            ROLE.MORDRED -> R.drawable.mordred_placeholder_c
            ROLE.OBERON -> R.drawable.oberon_placeholder_c
            ROLE.MINION_OF_MORDRED -> R.drawable.minion_of_mordred_c
        })

        binding.tvDescription.text = when(info.name){
            ROLE.PERCIVAL -> getString(R.string.precival_description)
            ROLE.MERLIN -> getString(R.string.merlin_description)
            ROLE.SERVANT_OF_ARTHUR -> getString(R.string.servant_of_arthur_description)
            ROLE.ARNOLD -> getString(R.string.arnold_description)
            ROLE.ASSASSIN -> getString(R.string.assassin_description)
            ROLE.MORGANA -> getString(R.string.morgana_description)
            ROLE.MORDRED -> getString(R.string.mordred_description)
            ROLE.OBERON -> getString(R.string.oberon_description)
            ROLE.MINION_OF_MORDRED -> getString(R.string.minion_of_mordred_description)
        }

        adapter.setItems(info.sees)

        binding.ivRole.setImageResource(when(info.name.isEvil()){
            true -> R.drawable.evil_c
            false -> R.drawable.good_c
        })

        binding.tvSees.text = when(info.name){
            ROLE.PERCIVAL -> getString(R.string.percival_sees)
            ROLE.MERLIN -> getString(R.string.merlin_sees)
            ROLE.ASSASSIN -> getString(R.string.evil_sees)
            ROLE.MORGANA -> getString(R.string.evil_sees)
            ROLE.MORDRED -> getString(R.string.evil_sees)
            ROLE.OBERON -> getString(R.string.evil_sees)
            ROLE.MINION_OF_MORDRED -> getString(R.string.evil_sees)
            else -> {
                binding.lAssassinList.playersList.visibility = View.GONE
                ""
            }

        }
    }

    fun setVisibility(visible:Boolean){
        val v = if(visible) View.VISIBLE else View.GONE
        binding.ivRole.visibility = v
        binding.tvDescription.visibility = v
        binding.tvCharacterName.visibility = v
        binding.ivCharacter.visibility = v
    }
}