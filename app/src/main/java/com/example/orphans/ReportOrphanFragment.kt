package com.example.orphans

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.orphans.databinding.FragmentReportOrphanBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ReportOrphanFragment : Fragment() {

    private lateinit var binding: FragmentReportOrphanBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var photoUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri = result.data?.data
            if (photoUri != null) {
                binding.imageViewPhotoPreview.setImageURI(photoUri)
                binding.imageCard.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_orphan, container, false)
        setupSpinners()

        binding.buttonUploadPhoto.setOnClickListener {
            pickImage()
        }

        binding.buttonSubmitReport.setOnClickListener {
            submitReport()
        }

        return binding.root
    }

    private fun setupSpinners() {
        val districts = resources.getStringArray(R.array.districts_array).toList()
        val states = resources.getStringArray(R.array.state_array).toList()

        binding.districtSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, districts)
        binding.stateSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, states)
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun submitReport() {
        val town = binding.editTextTown.text.toString().trim()
        val district = binding.districtSpinner.selectedItem?.toString() ?: ""
        val state = binding.stateSpinner.selectedItem?.toString() ?: ""

        if (town.isEmpty()) {
            binding.editTextTown.error = "Required"
            return
        }

        setLoading(true)

        if (photoUri != null) {
            val base64Image = uriToBase64(photoUri!!)
            if (base64Image != null) {
                submitReportToFirestore(town, state, district, base64Image)
            } else {
                setLoading(false)
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        } else {
            submitReportToFirestore(town, state, district, null)
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSubmitReport.isEnabled = !isLoading
        binding.buttonSubmitReport.text = if (isLoading) "Submitting..." else "Submit Report"
    }

    private fun submitReportToFirestore(town: String, state: String, district: String, photoBase64: String?) {
        val reportData = hashMapOf(
            "town" to town,
            "state" to state,
            "district" to district,
            "photoUrl" to photoBase64,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("orphan_reports").add(reportData)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Submitted successfully!", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputs() {
        binding.editTextTown.text?.clear()
        binding.imageCard.visibility = View.GONE
        photoUri = null
    }
}