package com.example.orphans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedbackAdapter(
    private val feedbackList: List<Feedback>,
    private val onDeleteClick: (Feedback) -> Unit
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val feedbackTextView: TextView = itemView.findViewById(R.id.feedbackTextView)
        private val itemRatingBar: RatingBar = itemView.findViewById(R.id.itemRatingBar)
        private val userRoleTextView: TextView = itemView.findViewById(R.id.userRoleTextView)
        private val userEmailTextView: TextView = itemView.findViewById(R.id.userEmailTextView)

        fun bind(feedback: Feedback) {
            feedbackTextView.text = feedback.feedback
            itemRatingBar.rating = feedback.rating
            userRoleTextView.text = feedback.userRole
            userEmailTextView.text = feedback.userEmail

            itemView.findViewById<View>(R.id.deleteButton).setOnClickListener {
                onDeleteClick(feedback)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.bind(feedbackList[position])
    }

    override fun getItemCount(): Int = feedbackList.size
}