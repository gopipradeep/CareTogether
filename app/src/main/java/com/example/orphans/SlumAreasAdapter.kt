package com.example.orphans

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SlumAreasAdapter(private val slumList: List<SlumArea>) :
    RecyclerView.Adapter<SlumAreasAdapter.SlumAreaViewHolder>() {

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
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewSlumArea)

        fun bind(slumArea: SlumArea) {
            textViewTown.text = slumArea.town
            textViewPopulation.text = "Expected Population: ${slumArea.expected_population}"

            val imageSource = slumArea.imageUrl
            if (!imageSource.isNullOrEmpty()) {
                if (imageSource.startsWith("http")) {
                    Glide.with(itemView.context)
                        .load(imageSource)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(imageView)
                } else {
                    try {
                        val imageByteArray = Base64.decode(imageSource, Base64.DEFAULT)
                        Glide.with(itemView.context)
                            .asBitmap()
                            .load(imageByteArray)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(imageView)
                    } catch (e: Exception) {
                        imageView.setImageResource(R.drawable.placeholder_image)
                    }
                }
            } else {
                imageView.setImageResource(R.drawable.placeholder_image)
            }
        }
    }
}