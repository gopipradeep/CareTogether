package com.example.orphans

import SlumArea
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class SlumAreasFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var slumAdapter: SlumAreaAdapter
    private lateinit var noDataTextView: TextView
    private var slumList: MutableList<SlumArea> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_slum_areas, container, false)

        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewSlumAreas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        slumAdapter = SlumAreaAdapter(slumList)
        recyclerView.adapter = slumAdapter

        noDataTextView = view.findViewById(R.id.textViewNoSlumAreas)

        val spinnerState: Spinner = view.findViewById(R.id.spinnerState)
        val states = listOf("Andhra Pradesh")
        val stateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerState.adapter = stateAdapter

        val spinnerDistrict: Spinner = view.findViewById(R.id.spinnerDistrict)
        val districts = listOf(
            "Alluri Sitharama Raju", "Anakapalli", "Ananthapuramu",
            "Annamayya", "Bapatla", "Chittoor", "Dr. B.R. Ambedkar Konaseema",
            "East Godavari", "Eluru", "Guntur", "Kakinada", "Krishna",
            "Kurnool", "Nandyal", "Ntr", "Palnadu", "Parvathipuram Manyam",
            "Prakasam", "Sri Potti Sriramulu Nellore", "Sri Sathya Sai",
            "Srikakulam", "Tirupati", "Visakhapatnam", "Vizianagaram",
            "West Godavari", "Y.S.R."
        )
        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts)
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistrict.adapter = districtAdapter

        val buttonSearch: Button = view.findViewById(R.id.buttonSearch)
        buttonSearch.setOnClickListener {
            val selectedState = spinnerState.selectedItem.toString()
            val selectedDistrict = spinnerDistrict.selectedItem.toString()

            if (selectedState.isNotEmpty() && selectedDistrict.isNotEmpty()) {
                fetchSlumAreas(selectedState, selectedDistrict)
            } else {
                Toast.makeText(requireContext(), "Please select both state and district", Toast.LENGTH_SHORT).show()
            }
        }

        val fabAdd: FloatingActionButton = view.findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), SlumAreaReportActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun fetchSlumAreas(state: String, district: String) {
        firestore.collection("slum_area")
            .whereEqualTo("state", state)
            .whereEqualTo("district", district)
            .get()
            .addOnSuccessListener { documents ->
                slumList.clear()
                if (documents.isEmpty) {
                    noDataTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    noDataTextView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    for (document in documents) {
                        val slum = document.toObject(SlumArea::class.java)
                        slumList.add(slum)
                        Log.d("SlumAreasFragment", "Fetched slum area: $slum")
                    }
                    slumAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("SlumAreasFragment", "Error fetching data", e)
            }
    }
}
