package com.example.orphans

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class OrphanReportsAdapter : RecyclerView.Adapter<OrphanReportsAdapter.ReportViewHolder>() {

    private var reports = listOf<OrphanReport>()

    fun submitList(newReports: List<OrphanReport>) {
        reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_orphan_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val townTextView: TextView = view.findViewById(R.id.text_town)
        private val locationTextView: TextView = view.findViewById(R.id.text_location_details)
        private val photoImageView: ImageView = view.findViewById(R.id.image_photo)

        fun bind(report: OrphanReport) {
            townTextView.text = report.town
            locationTextView.text = "${report.district}, ${report.state}"

            val imageSource = report.photoUrl
            if (!imageSource.isNullOrEmpty()) {
                if (imageSource.startsWith("http")) {
                    // It's a URL (Old data or still using Storage)
                    Glide.with(photoImageView.context)
                        .load(imageSource)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(photoImageView)
                } else {
                    // It's a Base64 string
                    try {
                        val imageByteArray = Base64.decode(imageSource, Base64.DEFAULT)
                        Glide.with(photoImageView.context)
                            .asBitmap()
                            .load(imageByteArray)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(photoImageView)
                    } catch (e: Exception) {
                        photoImageView.setImageResource(R.drawable.placeholder_image)
                    }
                }
            } else {
                photoImageView.setImageResource(R.drawable.placeholder_image)
            }
        }
    }
}
