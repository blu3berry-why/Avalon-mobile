package hu.blu3berry.avalon.view.lobby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.R
import hu.blu3berry.avalon.databinding.LobbySettingsBinding
import hu.blu3berry.avalon.model.network.Settings
import hu.blu3berry.avalon.utils.Status
import hu.blu3berry.avalon.utils.UserUtils
import javax.inject.Inject

@AndroidEntryPoint
class LobbySettingsFragment:Fragment() {
    private lateinit var binding: LobbySettingsBinding
    private val viewModel:LobbySettingsViewModel by viewModels()
    @Inject
    lateinit var userUtils: UserUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = LobbySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)

        setVisibility(false)
        getData()

        binding.btnSaveSettings.setOnClickListener {
            viewModel.setSettings(userUtils.lobbyCode!!, setData()).observe(viewLifecycleOwner){
                when(it.status){
                    Status.SUCCESS -> {
                        navController.navigate(R.id.on_back_to_lobby_main)
                    }
                }
            }

        }
    }
    fun getData(){
        viewModel.getSettings(userUtils.lobbyCode!!).observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS -> {
                    setVisibility(true)
                    val settings = it.data!!
                    binding.swArnold.isChecked = settings.arnold
                    binding.swAssassin.isChecked = settings.assassin
                    binding.swMorgana.isChecked = settings.morgana
                    binding.swMorderd.isChecked = settings.mordred
                    binding.swOberon.isChecked = settings.oberon
                    binding.swPercival.isChecked = settings.percival
                }
            }
        }
    }

    fun setData():Settings =  Settings(
            binding.swAssassin.isChecked,
            binding.swMorderd.isChecked,
            binding.swMorgana.isChecked,
            binding.swOberon.isChecked,
            binding.swPercival.isChecked,
            binding.swArnold.isChecked
        )

    fun setVisibility(to:Boolean){
        if(to){
            binding.swArnold.visibility = View.VISIBLE
            binding.swAssassin.visibility = View.VISIBLE
            binding.swMorderd.visibility = View.VISIBLE
            binding.swMorgana.visibility = View.VISIBLE
            binding.swOberon.visibility = View.VISIBLE
            binding.swPercival.visibility = View.VISIBLE
        }else{
            binding.swArnold.visibility = View.GONE
            binding.swAssassin.visibility = View.GONE
            binding.swMorderd.visibility = View.GONE
            binding.swMorgana.visibility = View.GONE
            binding.swOberon.visibility = View.GONE
            binding.swPercival.visibility = View.GONE
        }

    }
}