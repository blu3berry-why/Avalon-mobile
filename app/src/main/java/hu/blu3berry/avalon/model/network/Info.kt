package hu.blu3berry.avalon.model.network

import com.example.model.enums.SCORE
import com.example.model.enums.WINNER


data class Info(
    var started :Boolean,
    var winner: WINNER = WINNER.NOT_DECIDED,
    var scores: MutableList<SCORE>,
    var currentRound: Int,
    var isAdventure: Boolean,
    var currentAdventure: Int,
    var king: String?,
    var failCounter: Int = 0,
    var selectedForAdventure: MutableList<String>,
    var playersName: MutableList<String>,
    var assassinHasGuessed:Boolean = false,
    var playerSelectNum: Int = 0,
)
