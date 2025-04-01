package com.example.orphans

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val town: String = "",
    val district: String = "",
    val state: String = "",
    val profileImageUrl: String? = null
)
