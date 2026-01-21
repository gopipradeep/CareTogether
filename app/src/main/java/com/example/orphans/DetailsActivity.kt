package com.example.orphans

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.orphans.databinding.ActivityDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_details)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupUI()
        handleBackPress()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Logic: If user presses back without completing details, delete the auth account
                // so they have to "re sign up from the first" as requested.
                val user = auth.currentUser
                user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@DetailsActivity, "Sign up cancelled. Please register again.", Toast.LENGTH_SHORT).show()
                    } else {
                        auth.signOut()
                    }
                    startActivity(Intent(this@DetailsActivity, AuthActivity::class.java))
                    finish()
                } ?: run {
                    startActivity(Intent(this@DetailsActivity, AuthActivity::class.java))
                    finish()
                }
            }
        })
    }

    private fun setupUI() {
        // Setup Role Spinner
        val roles = listOf("Donor", "Organization")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roleSpinner.adapter = roleAdapter

        // Setup District Spinner
        val districts = resources.getStringArray(R.array.districts_array)
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, districts)
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.districtSpinner.adapter = districtAdapter

        // Setup State Spinner
        val states = resources.getStringArray(R.array.state_array)
        val stateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateSpinner.adapter = stateAdapter

        // Pre-fill email from Auth if available
        binding.emailEditText.setText(auth.currentUser?.email)

        binding.saveButton.setOnClickListener { saveDetails() }
    }

    private fun saveDetails() {
        val name = binding.nameEditText.text.toString().trim()
        val contactNumber = binding.contactNumberEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val town = binding.townEditText.text.toString().trim()
        val district = binding.districtSpinner.selectedItem.toString()
        val state = binding.stateSpinner.selectedItem.toString()
        val role = binding.roleSpinner.selectedItem.toString()

        if (name.isEmpty()) {
            binding.nameEditText.error = "Name is required"
            return
        }
        if (contactNumber.isEmpty()) {
            binding.contactNumberEditText.error = "Contact number is required"
            return
        }
        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            return
        }
        if (town.isEmpty()) {
            binding.townEditText.error = "Town is required"
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setLoading(true)

        val userProfile = UserProfile(
            name = name,
            email = email,
            contactNumber = contactNumber,
            town = town,
            district = district,
            state = state,
            role = role,
            profileImageUrl = null
        )

        firestore.collection("users").document(userId).set(userProfile)
            .addOnSuccessListener {
                UserCache.saveUserProfile(this, userProfile)
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                navigateToRoleActivity(role)
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Failed to save details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.saveButton.isEnabled = !isLoading
        binding.saveButton.text = if (isLoading) "Processing..." else "Create Account"
    }

    private fun navigateToRoleActivity(role: String) {
        val intent = when (role) {
            "Donor" -> Intent(this, DonorActivity::class.java)
            "Organization" -> Intent(this, OrganizationActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}