package com.example.orphans

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class OrphanagesFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var orphanageRecyclerView: RecyclerView
    private lateinit var orphanageAdapter: OrphanageAdapter
    private var orphanageList: MutableList<Orphanage> = mutableListOf()
    private lateinit var noOrphanagesTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_orphanages, container, false)

        firestore = FirebaseFirestore.getInstance()

        orphanageRecyclerView = view.findViewById(R.id.orphanageRecyclerView)
        orphanageList = mutableListOf()
        orphanageAdapter = OrphanageAdapter(orphanageList)
        orphanageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        orphanageRecyclerView.adapter = orphanageAdapter

        noOrphanagesTextView = view.findViewById(R.id.textViewNoOrphanages)

        val spinnerState: Spinner = view.findViewById(R.id.spinnerState)
        val spinnerDistrict: Spinner = view.findViewById(R.id.spinnerDistrict)

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

        val stateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerState.adapter = stateAdapter

        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts)
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistrict.adapter = districtAdapter

        val buttonSearch: Button = view.findViewById(R.id.buttonSearch)
        buttonSearch.setOnClickListener {
            val selectedState = spinnerState.selectedItem.toString()
            val selectedDistrict = spinnerDistrict.selectedItem.toString()

            if (selectedState.isNotEmpty() && selectedDistrict.isNotEmpty()) {
                searchOrphanages(selectedState, selectedDistrict, "Organization")
            } else {
                Toast.makeText(requireContext(), "Please select both state and district", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun searchOrphanages(state: String, district: String, role: String) {
        orphanageList.clear()

        firestore.collection("users")
            .whereEqualTo("role", role)
            .whereEqualTo("state", state)
            .whereEqualTo("district", district)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    orphanageList.clear()
                    for (document in documents) {
                        val name = document.getString("name") ?: "Unnamed Orphanage"
                        val contactNumber = document.getString("contactNumber") ?: "No Contact Info"
                        val email = document.getString("email") ?: "No Email Provided"
                        val town = document.getString("town") ?: "Not Specified"

                        fetchNeeds(name, contactNumber, email, town)
                    }
                } else {
                    noOrphanagesTextView.visibility = View.VISIBLE
                    orphanageRecyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchNeeds(orphanageName: String, contactNumber: String, email: String, town: String) {
        firestore.collection("needs")
            .whereEqualTo("organizationname", orphanageName)
            .whereEqualTo("contactNumber", contactNumber)
            .get()
            .addOnSuccessListener { needDocuments ->
                val needsList = mutableListOf<String>()
                for (needDocument in needDocuments) {
                    val needDescription = needDocument.getString("description") ?: "No description available"
                    needsList.add(needDescription)
                }

                val orphanage = Orphanage(orphanageName, contactNumber, email, town, needsList)
                orphanageList.add(orphanage)
                orphanageAdapter.notifyDataSetChanged()

                noOrphanagesTextView.visibility = View.GONE
                orphanageRecyclerView.visibility = View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Log.e("OrphanagesFragment", "Error fetching needs: ${exception.message}")
                val orphanage = Orphanage(orphanageName, contactNumber, email, town, emptyList())
                orphanageList.add(orphanage)
                orphanageAdapter.notifyDataSetChanged()
            }
    }
}
