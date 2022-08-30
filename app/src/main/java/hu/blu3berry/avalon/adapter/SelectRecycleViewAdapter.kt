package hu.blu3berry.avalon.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.blu3berry.avalon.databinding.GameSelectRowBinding

class SelectRecycleViewAdapter: RecyclerView.Adapter<SelectRecycleViewAdapter.PlayerSelectViewHolder>() {

    private val items = mutableListOf<String>()



    var selected:MutableList<String> = mutableListOf()


    fun setItems(newItems: List<String>){
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerSelectViewHolder {
        val binding = GameSelectRowBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return PlayerSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerSelectViewHolder, position: Int) {
        holder.binding.tvPlayerNameSelect.text = items[position]
        holder.binding.cbSelected.setOnCheckedChangeListener { compoundButton, b ->
            if(holder.binding.cbSelected.isChecked){
                selected.add( holder.binding.tvPlayerNameSelect.text.toString())
            }else{
                selected.remove(holder.binding.tvPlayerNameSelect.text.toString())
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class PlayerSelectViewHolder(val binding: GameSelectRowBinding ): RecyclerView.ViewHolder(binding.root){
        lateinit var currentPlayer: String
    }
}