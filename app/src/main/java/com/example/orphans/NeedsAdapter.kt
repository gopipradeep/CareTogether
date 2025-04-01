package com.example.orphans

import Need
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NeedsAdapter(
    private val needs: List<Need>,
    private val onDelete: (Need) -> Unit
) : RecyclerView.Adapter<NeedsAdapter.NeedViewHolder>() {

    inner class NeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val needDescription: TextView = itemView.findViewById(R.id.needDescription)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(need: Need) {
            needDescription.text = need.description
            deleteButton.setOnClickListener { onDelete(need) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_need, parent, false)
        return NeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NeedViewHolder, position: Int) {
        holder.bind(needs[position])
    }

    override fun getItemCount(): Int = needs.size
}
