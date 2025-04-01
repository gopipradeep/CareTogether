package com.example.orphans

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.orphans.databinding.ActivityDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_details)

        firestore = FirebaseFirestore.getInstance()

        checkUserDetails()
    }

    private fun checkUserDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        populateUserDetails(
                            document.getString("name"), document.getString("contactNumber"),
                            document.getString("email"), document.getString("town"),
                            document.getString("district"), document.getString("state"),
                            document.getString("role")
                        )
                    } else {
                        setupUI()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DetailsActivity", "Error checking user details", e)
                    Toast.makeText(this, "Error checking user details", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Log.e("DetailsActivity", "User not authenticated")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateUserDetails(
        name: String?, contactNumber: String?, email: String?, town: String?,
        district: String?, state: String?, role: String?
    ) {
        binding.apply {
            nameEditText.setText(name)
            contactNumberEditText.setText(contactNumber)
            emailEditText.setText(email)
            townEditText.setText(town)
            districtSpinner.setSelection(getDistrictIndex(district ?: ""))
            stateSpinner.setSelection(getStateIndex(state ?: ""))
            roleSpinner.setSelection(getRoleIndex(role ?: ""))
        }
        setupUI()
    }

    private fun setupUI() {
        setupRoleSpinner()
        setupDistrictSpinner()
        setupStateSpinner()

        binding.saveButton.setOnClickListener { saveDetails() }
    }

    private fun setupRoleSpinner() {
        val roles = listOf("Donor", "Organization")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roleSpinner.adapter = adapter
        Log.d("DetailsActivity", "Role Spinner set up")
    }

    private fun setupDistrictSpinner() {
        val districts = resources.getStringArray(R.array.districts_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, districts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.districtSpinner.adapter = adapter
    }

    private fun setupStateSpinner() {
        val states = resources.getStringArray(R.array.state_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateSpinner.adapter = adapter
    }

    private fun saveDetails() {
        val name = binding.nameEditText.text.toString().trim()
        val contactNumber = binding.contactNumberEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val town = binding.townEditText.text.toString().trim()
        val district = binding.districtSpinner.selectedItem.toString()
        val state = binding.stateSpinner.selectedItem.toString()
        val role = binding.roleSpinner.selectedItem.toString()

        if (validateInput(name, contactNumber, email, town)) {
            val userMap = hashMapOf(
                "name" to name,
                "contactNumber" to contactNumber,
                "email" to email,
                "town" to town,
                "district" to district,
                "state" to state,
                "role" to role
            )
            saveUserToFirestore(userMap, role)
        }
    }

    private fun validateInput(name: String, contactNumber: String, email: String, town: String): Boolean {
        if (name.isEmpty() || contactNumber.isEmpty() || email.isEmpty() || town.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveUserToFirestore(userMap: HashMap<String, String>, role: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Details saved successfully", Toast.LENGTH_SHORT).show()
                navigateToRoleBasedActivity(role)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToRoleBasedActivity(role: String) {
        val intent = when (role) {
            "Donor" -> Intent(this, DonorActivity::class.java)
            "Organization" -> Intent(this, OrganizationActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun getDistrictIndex(district: String): Int {
        return resources.getStringArray(R.array.districts_array).indexOf(district).takeIf { it >= 0 } ?: 0
    }

    private fun getStateIndex(state: String): Int {
        return resources.getStringArray(R.array.state_array).indexOf(state).takeIf { it >= 0 } ?: 0
    }

    private fun getRoleIndex(role: String): Int {
        return listOf("Donor", "Organization").indexOf(role).takeIf { it >= 0 } ?: 0
    }
}
