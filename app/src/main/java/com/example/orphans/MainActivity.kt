package com.example.orphans

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        // Show welcome screen for 2 seconds before checking auth
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAuthentication()
        }, 2000)
    }

    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            retrieveUserRole(currentUser.uid)
        } else {
            navigateToAuth()
        }
    }

    private fun retrieveUserRole(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    if (role != null && role != "default") {
                        navigateToRoleActivity(role)
                    } else {
                        navigateToDetails()
                    }
                } else {
                    // Auth user exists but no Firestore profile yet
                    navigateToDetails()
                }
            }
            .addOnFailureListener { exception ->
                navigateToAuth()
            }
    }

    private fun navigateToRoleActivity(role: String?) {
        val intent = when (role) {
            "Donor" -> Intent(this, DonorActivity::class.java)
            "Organization" -> Intent(this, OrganizationActivity::class.java)
            else -> Intent(this, DetailsActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun navigateToDetails() {
        startActivity(Intent(this, DetailsActivity::class.java))
        finish()
    }
}