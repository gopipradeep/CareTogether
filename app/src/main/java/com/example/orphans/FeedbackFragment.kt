package com.example.orphans

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFragment : Fragment() {
    private lateinit var feedbackEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userRole: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)

        feedbackEditText = view.findViewById(R.id.editTextFeedback)
        submitButton = view.findViewById(R.id.buttonSubmitFeedback)
        ratingBar = view.findViewById(R.id.ratingBar)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchUserRole()

        submitButton.setOnClickListener {
            submitFeedback()
        }

        return view
    }

    private fun fetchUserRole() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("role")) {
                        userRole = document.getString("role")
                    } else {
                        Toast.makeText(requireContext(), "Role not found for user", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to fetch role: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitFeedback() {
        val feedback = feedbackEditText.text.toString()
        val rating = ratingBar.rating

        if (feedback.isNotEmpty() && userRole != null) {
            val feedbackData = hashMapOf(
                "feedback" to feedback,
                "userRole" to userRole,
                "rating" to rating
            )

            firestore.collection("feedbacks")
                .add(feedbackData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Feedback submitted", Toast.LENGTH_SHORT).show()
                    feedbackEditText.text.clear()
                    ratingBar.rating = 0f
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to submit feedback: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Please enter your feedback or check your role", Toast.LENGTH_SHORT).show()
        }
    }
}
