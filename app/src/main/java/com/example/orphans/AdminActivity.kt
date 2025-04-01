package com.example.orphans

import Feedback
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.orphans.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var feedbackRecyclerView: RecyclerView
    private lateinit var feedbackAdapter: FeedbackAdapter
    private val feedbackList = mutableListOf<Feedback>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        feedbackRecyclerView = findViewById(R.id.feedbackRecyclerView)
        feedbackRecyclerView.layoutManager = LinearLayoutManager(this)

        feedbackAdapter = FeedbackAdapter(feedbackList) { feedback -> deleteFeedback(feedback) }
        feedbackRecyclerView.adapter = feedbackAdapter

        loadFeedback()

        binding.logoutButton.setOnClickListener { logout() }
    }

    private fun loadFeedback() {
        firestore.collection("feedbacks")
            .orderBy("rating", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                feedbackList.clear() // Clear the list before loading new data
                for (document in documents) {
                    val feedback = document.toObject(Feedback::class.java)
                    feedback.feedbackId = document.id
                    feedbackList.add(feedback)
                }
                feedbackAdapter.notifyDataSetChanged()
                Log.d("AdminActivity", "Loaded feedback count: ${documents.size()}")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFeedback(feedback: Feedback) {

        firestore.collection("feedbacks").document(feedback.feedbackId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Feedback deleted successfully", Toast.LENGTH_SHORT).show()
                feedbackList.remove(feedback)
                feedbackAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}
