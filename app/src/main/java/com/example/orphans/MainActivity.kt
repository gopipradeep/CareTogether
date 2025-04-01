package com.example.orphans

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.orphans.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            retrieveUserRole(currentUser.uid)
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun retrieveUserRole(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    navigateToRoleActivity(role)
                } else {
                    startActivity(Intent(this, AuthActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
    }

    private fun navigateToRoleActivity(role: String?) {
        when (role) {
            "Donor" -> startActivity(Intent(this, DonorActivity::class.java))
            "Organization" -> startActivity(Intent(this, OrganizationActivity::class.java))
            else -> startActivity(Intent(this, AuthActivity::class.java)) // Fallback
        }
        finish()
    }
}
