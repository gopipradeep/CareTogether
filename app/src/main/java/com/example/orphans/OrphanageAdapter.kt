package com.example.orphans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrphanageAdapter(
    private val orphanages: List<Orphanage>
) : RecyclerView.Adapter<OrphanageAdapter.OrphanageViewHolder>() {

    class OrphanageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orphanageName: TextView = itemView.findViewById(R.id.orphanageName)
        val orphanageContact: TextView = itemView.findViewById(R.id.orphanageContact)
        val orphanageEmail: TextView = itemView.findViewById(R.id.orphanageEmail)
        val orphanageTown: TextView = itemView.findViewById(R.id.orphanageTown)
        val orphanageNeeds: TextView = itemView.findViewById(R.id.orphanageNeeds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrphanageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_orphanage, parent, false)
        return OrphanageViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrphanageViewHolder, position: Int) {
        val orphanage = orphanages[position]
        holder.orphanageName.text = orphanage.name
        holder.orphanageTown.text = "Town: ${orphanage.town}"
        holder.orphanageContact.text = "Contact: ${orphanage.contactNumber}"
        holder.orphanageEmail.text = "Email: ${orphanage.email}"

        val needsText = if (orphanage.needs.isNotEmpty()) {
            "Needs: ${orphanage.needs.joinToString(", ")}"
        } else {
            "No needs available."
        }
        holder.orphanageNeeds.text = needsText
    }

    override fun getItemCount(): Int {
        return orphanages.size
    }

}
