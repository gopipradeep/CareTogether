package com.example.orphans

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.orphans.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null

    private val states = listOf("Andhra Pradesh", "Karnataka", "Maharashtra") // Add all states here
    private val districts = mapOf(
        "Andhra Pradesh" to listOf(
            "Alluri Sitharama Raju", "Anakapalli", "Ananthapuramu",
            "Annamayya", "Bapatla", "Chittoor", "Dr. B.R. Ambedkar Konaseema",
            "East Godavari", "Eluru", "Guntur", "Kakinada", "Krishna",
            "Kurnool", "Nandyal", "Ntr", "Palnadu", "Parvathipuram Manyam",
            "Prakasam", "Sri Potti Sriramulu Nellore", "Sri Sathya Sai",
            "Srikakulam", "Tirupati", "Visakhapatnam", "Vizianagaram",
            "West Godavari", "Y.S.R."
        )
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserProfile()
        setupStateSpinner()

        binding.buttonEditProfile.setOnClickListener { enableEditing(true) }
        binding.buttonSaveProfile.setOnClickListener { saveProfileData() }
        binding.buttonLogout.setOnClickListener { logout() }

        binding.imageViewProfile.setOnClickListener {
            if (binding.buttonSaveProfile.visibility == View.VISIBLE) {
                pickProfileImage()
            }
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                profile?.let { displayUserProfile(it) }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayUserProfile(profile: UserProfile) {
        binding.editTextName.setText(profile.name)
        binding.editTextEmail.setText(profile.email)
        binding.editTextPhone.setText(profile.contactNumber)
        binding.editTextTown.setText(profile.town)

        binding.textViewState.text = profile.state
        binding.textViewDistrict.text = profile.district

        setupDistrictSpinner(profile.state)

        profile.profileImageUrl?.let {
            Glide.with(this).load(it).into(binding.imageViewProfile)
        }
    }

    private fun setupStateSpinner() {
        val stateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerState.adapter = stateAdapter

        binding.spinnerState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedState = states[position]
                setupDistrictSpinner(selectedState)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun setupDistrictSpinner(selectedState: String) {
        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts[selectedState] ?: emptyList())
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistrict.adapter = districtAdapter
    }

    private fun enableEditing(enable: Boolean) {
        binding.editTextName.isEnabled = enable
        binding.editTextEmail.isEnabled = enable
        binding.editTextPhone.isEnabled = enable
        binding.editTextTown.isEnabled = enable
        binding.spinnerState.isEnabled = enable
        binding.spinnerDistrict.isEnabled = enable
        binding.buttonSaveProfile.visibility = if (enable) View.VISIBLE else View.GONE

        if (enable) {
            binding.textViewState.visibility = View.GONE
            binding.textViewDistrict.visibility = View.GONE
            binding.spinnerState.visibility = View.VISIBLE
            binding.spinnerDistrict.visibility = View.VISIBLE
        } else {
            binding.textViewState.visibility = View.VISIBLE
            binding.textViewDistrict.visibility = View.VISIBLE
            binding.spinnerState.visibility = View.GONE
            binding.spinnerDistrict.visibility = View.GONE
        }

        binding.imageViewProfile.isClickable = enable
        binding.imageViewProfile.alpha = if (enable) 1.0f else 0.5f
    }

    private fun saveProfileData() {
        val userId = auth.currentUser?.uid ?: return
        val selectedState = binding.spinnerState.selectedItem.toString()
        val selectedDistrict = binding.spinnerDistrict.selectedItem.toString()
        val profileData = mapOf(
            "name" to binding.editTextName.text.toString().trim(),
            "email" to binding.editTextEmail.text.toString().trim(),
            "contactNumber" to binding.editTextPhone.text.toString().trim(),
            "district" to selectedDistrict,
            "town" to binding.editTextTown.text.toString().trim(),
            "state" to selectedState
        )

        firestore.collection("users").document(userId).update(profileData)
            .addOnSuccessListener {
                if (selectedImageUri != null) {
                    uploadProfileImage(userId)
                } else {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    enableEditing(false)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfileImage(userId: String) {
        val ref = storage.reference.child("profile_images/$userId.jpg")
        selectedImageUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        firestore.collection("users").document(userId)
                            .update("profileImageUrl", downloadUri.toString())
                            .addOnSuccessListener {
                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                enableEditing(false)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to update image URL", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun pickProfileImage() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.imageViewProfile.setImageURI(selectedImageUri)
        }
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(context, AuthActivity::class.java))
        requireActivity().finish()
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}
