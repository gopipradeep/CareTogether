package com.example.orphans

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedImageUri: Uri? = null

    private val states = listOf("Andhra Pradesh", "Karnataka", "Maharashtra")
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

        // Instant Loading: Show cached data first
        showCachedProfile()
        
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

    private fun showCachedProfile() {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val cachedProfileJson = sharedPref.getString("user_profile", null)
        if (cachedProfileJson != null) {
            val profile = Gson().fromJson(cachedProfileJson, UserProfile::class.java)
            displayUserProfile(profile)
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                profile?.let { 
                    displayUserProfile(it)
                    // Cache the fresh data
                    cacheProfile(it)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cacheProfile(profile: UserProfile) {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_profile", Gson().toJson(profile))
            apply()
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

        val imageSource = profile.profileImageUrl
        if (!imageSource.isNullOrEmpty()) {
            if (imageSource.startsWith("http")) {
                Glide.with(this).load(imageSource).placeholder(R.drawable.placeholder_image).into(binding.imageViewProfile)
            } else {
                try {
                    val imageByteArray = Base64.decode(imageSource, Base64.DEFAULT)
                    Glide.with(this).asBitmap().load(imageByteArray).placeholder(R.drawable.placeholder_image).into(binding.imageViewProfile)
                } catch (e: Exception) {
                    binding.imageViewProfile.setImageResource(R.drawable.placeholder_image)
                }
            }
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
            override fun onNothingSelected(parent: AdapterView<*>) {}
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
        binding.buttonEditProfile.visibility = if (enable) View.GONE else View.VISIBLE

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

        binding.imageViewProfile.alpha = if (enable) 0.8f else 1.0f
    }

    private fun saveProfileData() {
        val userId = auth.currentUser?.uid ?: return
        
        var base64Image: String? = null
        if (selectedImageUri != null) {
            base64Image = uriToBase64(selectedImageUri!!)
        }

        val profileData = mutableMapOf(
            "name" to binding.editTextName.text.toString().trim(),
            "email" to binding.editTextEmail.text.toString().trim(),
            "contactNumber" to binding.editTextPhone.text.toString().trim(),
            "town" to binding.editTextTown.text.toString().trim(),
            "state" to binding.spinnerState.selectedItem.toString(),
            "district" to binding.spinnerDistrict.selectedItem.toString()
        )

        if (base64Image != null) {
            profileData["profileImageUrl"] = base64Image
        }

        firestore.collection("users").document(userId).update(profileData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                enableEditing(false)
                loadUserProfile()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            null
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
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().remove("user_profile").apply()
        startActivity(Intent(context, AuthActivity::class.java))
        requireActivity().finish()
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}
