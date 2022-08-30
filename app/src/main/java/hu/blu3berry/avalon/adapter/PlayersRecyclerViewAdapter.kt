package hu.blu3berry.avalon.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.blu3berry.avalon.databinding.LobbyPlayerRowBinding

class PlayersRecyclerViewAdapter: RecyclerView.Adapter<PlayersRecyclerViewAdapter.PlayerViewHolder>() {

    private val items = mutableListOf<String>()


    fun setItems(newItems: List<String>){
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = LobbyPlayerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.binding.textView.text = items[position]
    }

    override fun getItemCount(): Int = items.size

    inner class PlayerViewHolder(val binding: LobbyPlayerRowBinding):RecyclerView.ViewHolder(binding.root){
        lateinit var currentPlayer: String
    }
}