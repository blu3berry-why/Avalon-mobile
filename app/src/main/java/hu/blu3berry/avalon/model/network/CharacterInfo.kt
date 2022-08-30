package hu.blu3berry.avalon.model.network

import com.example.model.enums.ROLE



data class CharacterInfo(
    var name: ROLE,
    val sees: MutableList<String>,
)
