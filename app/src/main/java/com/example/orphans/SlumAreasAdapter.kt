package com.example.orphans

import SlumArea
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SlumAreaAdapter(private val slumList: List<SlumArea>) :
    RecyclerView.Adapter<SlumAreaAdapter.SlumAreaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlumAreaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slum_area, parent, false)
        return SlumAreaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlumAreaViewHolder, position: Int) {
        val slumArea = slumList[position]
        holder.bind(slumArea)
    }

    override fun getItemCount(): Int {
        return slumList.size
    }

    class SlumAreaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTown: TextView = itemView.findViewById(R.id.textViewTown)
        private val textViewPopulation: TextView = itemView.findViewById(R.id.textViewPopulation)
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewSlumArea) // Add ImageView

        fun bind(slumArea: SlumArea) {
            textViewTown.text = slumArea.town
            textViewPopulation.text = "Expected Population: ${slumArea.expected_population}"

            if (!slumArea.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(slumArea.imageUrl)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.placeholder_image) // Placeholder image
            }
        }
    }
}
