package com.example.orphans

import SlumArea
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class SlumAreaReportActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var townEditText: EditText
    private lateinit var populationEditText: EditText
    private lateinit var stateSpinner: Spinner
    private lateinit var districtSpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slum_area_report)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        townEditText = findViewById(R.id.editTextTown)
        populationEditText = findViewById(R.id.editTextPopulation)
        stateSpinner = findViewById(R.id.spinnerState)
        districtSpinner = findViewById(R.id.spinnerDistrict)
        submitButton = findViewById(R.id.buttonSubmit)
        uploadImageButton = findViewById(R.id.buttonUploadImage)
        imageView = findViewById(R.id.imageView)

        val states = listOf("Andhra Pradesh")
        val districts = listOf(
            "Alluri Sitharama Raju", "Anakapalli", "Ananthapuramu",
            "Annamayya", "Bapatla", "Chittoor", "Dr. B.R. Ambedkar Konaseema",
            "East Godavari", "Eluru", "Guntur", "Kakinada", "Krishna",
            "Kurnool", "Nandyal", "Ntr", "Palnadu", "Parvathipuram Manyam",
            "Prakasam", "Sri Potti Sriramulu Nellore", "Sri Sathya Sai",
            "Srikakulam", "Tirupati", "Visakhapatnam", "Vizianagaram",
            "West Godavari", "Y.S.R."
        )

        val stateAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, districts)
        stateSpinner.adapter = stateAdapter
        districtSpinner.adapter = districtAdapter

        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        submitButton.setOnClickListener {
            val town = townEditText.text.toString()
            val population = populationEditText.text.toString()
            val state = stateSpinner.selectedItem.toString()
            val district = districtSpinner.selectedItem.toString()

            if (town.isNotEmpty() && population.isNotEmpty()) {
                if (imageUri != null) {
                    uploadImageAndSaveData(town, population, state, district)
                } else {
                    Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImageAndSaveData(town: String, population: String, state: String, district: String) {
        val imageRef = storageRef.child("slum_area_images/${System.currentTimeMillis()}.jpg")

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveSlumAreaData(town, population, state, district, downloadUrl.toString())
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to get image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSlumAreaData(town: String, population: String, state: String, district: String, imageUrl: String?) {
        val slumArea = SlumArea(
            town = town,
            expected_population = population,
            state = state,
            district = district,
            imageUrl = imageUrl
        )

        firestore.collection("slum_area")
            .add(slumArea)
            .addOnSuccessListener {
                Toast.makeText(this, "Slum area reported successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to report slum area: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
