package com.example.orphans

data class Orphanage(
    val name: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val town: String = "",
    var needs: List<String> = listOf()
)

