package com.example.orphans

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class OrphanageAdapter(
    private val orphanages: List<Orphanage>
) : RecyclerView.Adapter<OrphanageAdapter.OrphanageViewHolder>() {

    class OrphanageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orphanageName: TextView = itemView.findViewById(R.id.orphanageName)
        val orphanageContact: TextView = itemView.findViewById(R.id.orphanageContact)
        val orphanageEmail: TextView = itemView.findViewById(R.id.orphanageEmail)
        val orphanageTown: TextView = itemView.findViewById(R.id.orphanageTown)
        val orphanageNeeds: TextView = itemView.findViewById(R.id.orphanageNeeds)
        val imageViewOrphanage: ImageView = itemView.findViewById(R.id.imageViewOrphanage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrphanageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_orphanage, parent, false)
        return OrphanageViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrphanageViewHolder, position: Int) {
        val orphanage = orphanages[position]
        holder.orphanageName.text = orphanage.name
        holder.orphanageTown.text = orphanage.town
        holder.orphanageContact.text = orphanage.contactNumber
        holder.orphanageEmail.text = orphanage.email

        val needsText = if (orphanage.needs.isNotEmpty()) {
            "Needs: ${orphanage.needs.joinToString(", ")}"
        } else {
            "No needs listed."
        }
        holder.orphanageNeeds.text = needsText

        // Handle Image Loading
        val imageSource = orphanage.profileImageUrl
        if (!imageSource.isNullOrEmpty()) {
            if (imageSource.startsWith("http")) {
                Glide.with(holder.itemView.context)
                    .load(imageSource)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.imageViewOrphanage)
            } else {
                try {
                    val imageByteArray = Base64.decode(imageSource, Base64.DEFAULT)
                    Glide.with(holder.itemView.context)
                        .asBitmap()
                        .load(imageByteArray)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(holder.imageViewOrphanage)
                } catch (e: Exception) {
                    holder.imageViewOrphanage.setImageResource(R.drawable.placeholder_image)
                }
            }
        } else {
            holder.imageViewOrphanage.setImageResource(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount(): Int {
        return orphanages.size
    }
}