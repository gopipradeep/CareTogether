package com.example.orphans

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.orphans.databinding.ActivitySlumAreaReportBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class SlumAreaReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySlumAreaReportBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            if (imageUri != null) {
                binding.imageView.setImageURI(imageUri)
                binding.imageView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlumAreaReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()

        binding.buttonUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.buttonSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupSpinners() {
        val states = listOf("Andhra Pradesh")
        val districts = resources.getStringArray(R.array.districts_array).toList()

        binding.spinnerState.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, states)
        binding.spinnerDistrict.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, districts)
    }

    private fun validateAndSubmit() {
        val town = binding.editTextTown.text.toString().trim()
        val population = binding.editTextPopulation.text.toString().trim()
        val state = binding.spinnerState.selectedItem.toString()
        val district = binding.spinnerDistrict.selectedItem.toString()

        if (town.isEmpty()) {
            binding.editTextTown.error = "Town is required"
            return
        }
        if (population.isEmpty()) {
            binding.editTextPopulation.error = "Population is required"
            return
        }

        setLoading(true)

        if (imageUri != null) {
            val base64Image = uriToBase64(imageUri!!)
            if (base64Image != null) {
                saveSlumAreaData(town, population, state, district, base64Image)
            } else {
                setLoading(false)
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        } else {
            saveSlumAreaData(town, population, state, district, null)
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true)
            
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSubmit.isEnabled = !isLoading
        binding.buttonSubmit.text = if (isLoading) "Submitting..." else "Submit Report"
    }

    private fun saveSlumAreaData(town: String, population: String, state: String, district: String, imageBase64: String?) {
        val slumArea = SlumArea(
            town = town,
            expected_population = population,
            state = state,
            district = district,
            imageUrl = imageBase64
        )

        firestore.collection("slum_area")
            .add(slumArea)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, "Reported successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}