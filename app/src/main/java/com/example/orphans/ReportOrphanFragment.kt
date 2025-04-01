package com.example.orphans

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.orphans.databinding.FragmentReportOrphanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ReportOrphanFragment : Fragment() {

    private lateinit var binding: FragmentReportOrphanBinding
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var photoUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_orphan, container, false)

        setupSpinners()
        checkPermissions()

        binding.buttonUploadPhoto.setOnClickListener {
            pickImage()
        }

        binding.buttonSubmitReport.setOnClickListener {
            submitReport()
        }

        return binding.root
    }

    private fun setupSpinners() {
        val districts = listOf(
            "Alluri Sitharama Raju", "Anakapalli", "Ananthapuramu",
            "Annamayya", "Bapatla", "Chittoor", "Dr. B.R. Ambedkar Konaseema",
            "East Godavari", "Eluru", "Guntur", "Kakinada", "Krishna",
            "Kurnool", "Nandyal", "Ntr", "Palnadu", "Parvathipuram Manyam",
            "Prakasam", "Sri Potti Sriramulu Nellore", "Sri Sathya Sai",
            "Srikakulam", "Tirupati", "Visakhapatnam", "Vizianagaram",
            "West Godavari", "Y.S.R."
        )
        val states = listOf("Andhra Pradesh")

        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts)
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.districtSpinner.adapter = districtAdapter

        val stateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateSpinner.adapter = stateAdapter
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            photoUri = data?.data
            if (photoUri != null) {
                binding.imageViewPhotoPreview.setImageURI(photoUri)
                binding.imageViewPhotoPreview.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "No image selected.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitReport() {
        val town = binding.editTextTown.text.toString().trim()
        val district = binding.districtSpinner.selectedItem?.toString() ?: ""
        val state = binding.stateSpinner.selectedItem?.toString() ?: ""

        if (town.isEmpty() || district.isEmpty() || state.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (photoUri != null) {
            uploadPhotoAndSubmitReport(town, state, district)
        } else {
            submitReportToFirestore(town, state, district, null)
        }
    }

    private fun uploadPhotoAndSubmitReport(town: String, state: String, district: String) {
        val filename = "orphan_reports/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(filename)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "You need to be logged in to upload images.", Toast.LENGTH_SHORT).show()
            return
        }

        photoUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        submitReportToFirestore(town, state, district, downloadUri.toString())
                    }.addOnFailureListener { e ->
                        Log.e("UploadError", "Failed to get download URL: ${e.message}")
                        Toast.makeText(requireContext(), "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.e("UploadError", "Failed to upload photo: ${e.message}")
                    Toast.makeText(requireContext(), "Photo upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "No image selected for upload.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitReportToFirestore(town: String, state: String, district: String, photoUrl: String?) {
        val reportData = hashMapOf(
            "town" to town,
            "state" to state,
            "district" to district,
            "photoUrl" to photoUrl
        )

        firestore.collection("orphan_reports")
            .add(reportData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error adding document: ${e.message}")
                Toast.makeText(requireContext(), "Failed to submit report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputs() {
        binding.editTextTown.text.clear()
        binding.districtSpinner.setSelection(0)
        binding.stateSpinner.setSelection(0)
        binding.imageViewPhotoPreview.visibility = View.GONE
        photoUri = null
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_PERMISSION = 2
    }
}
