package hu.blu3berry.avalon.model.network


data class RoundVote(
    val king:String,
    val choosen: MutableList<String>,
    val results: MutableList <SingleVote>,
)
