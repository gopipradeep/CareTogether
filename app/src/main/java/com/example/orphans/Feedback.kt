package com.example.orphans

data class Feedback(
    val feedback: String = "",
    val userRole: String = "",
    val userEmail: String = "",
    val rating: Float = 0f,
    var feedbackId: String = ""
)