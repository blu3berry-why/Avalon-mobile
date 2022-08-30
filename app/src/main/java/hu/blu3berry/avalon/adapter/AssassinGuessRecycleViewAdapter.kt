package hu.blu3berry.avalon.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.blu3berry.avalon.databinding.GameAssassinGuessPlayerRowBinding
import hu.blu3berry.avalon.view.game.UserSelectedListener

class AssassinGuessRecycleViewAdapter(
    val listener: UserSelectedListener
): RecyclerView.Adapter<AssassinGuessRecycleViewAdapter.PlayerGuessViewHolder>() {
    private val items = mutableListOf<String>()

    fun setItems(newItems: List<String>){
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerGuessViewHolder {
        val binding = GameAssassinGuessPlayerRowBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return PlayerGuessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerGuessViewHolder, position: Int) {
        holder.binding.tvPlayerName.text = items[position]
        holder.binding.btnGuess.setOnClickListener {
            listener.onUserSelected(items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    inner class PlayerGuessViewHolder(val binding: GameAssassinGuessPlayerRowBinding): RecyclerView.ViewHolder(binding.root){
        lateinit var currentPlayer: String
    }
}