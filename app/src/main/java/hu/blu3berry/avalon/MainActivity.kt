package hu.blu3berry.avalon

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import hu.blu3berry.avalon.databinding.ActivityMainBinding
import hu.blu3berry.avalon.model.network.LoginInfo
import hu.blu3berry.avalon.repository.Repository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)


        }
}