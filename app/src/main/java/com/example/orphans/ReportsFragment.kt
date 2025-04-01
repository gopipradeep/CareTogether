package com.example.orphans

import OrphanReportsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orphans.databinding.FragmentReportsBinding
import com.google.firebase.firestore.FirebaseFirestore

class ReportsFragment : Fragment() {

    private lateinit var binding: FragmentReportsBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var orphanReportsAdapter: OrphanReportsAdapter

    private val districts = listOf(
        "Alluri Sitharama Raju", "Anakapalli", "Ananthapuramu", "Annamayya",
        "Bapatla", "Chittoor", "Dr. B.R. Ambedkar Konaseema", "East Godavari",
        "Eluru", "Guntur", "Kakinada", "Krishna", "Kurnool", "Nandyal", "Ntr",
        "Palnadu", "Parvathipuram Manyam", "Prakasam", "Sri Potti Sriramulu Nellore",
        "Sri Sathya Sai", "Srikakulam", "Tirupati", "Visakhapatnam", "Vizianagaram",
        "West Godavari", "Y.S.R."
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportsBinding.inflate(inflater, container, false)


        orphanReportsAdapter = OrphanReportsAdapter()
        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orphanReportsAdapter
        }

        setupStateSpinner()
        setupDistrictSpinner()

        binding.buttonSearch.setOnClickListener {
            val selectedState = binding.spinnerState.selectedItem?.toString()
            val selectedDistrict = binding.spinnerDistrict.selectedItem?.toString()

            if (selectedState != null && selectedDistrict != null) {
                loadOrphanReports(selectedState, selectedDistrict)
            } else {
                Toast.makeText(requireContext(), "Please select a district", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupStateSpinner() {
        val states = listOf("Andhra Pradesh")
        val stateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerState.adapter = stateAdapter

        binding.spinnerState.setSelection(0)
    }

    private fun setupDistrictSpinner() {
        val districtAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, districts)
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistrict.adapter = districtAdapter
    }

    private fun loadOrphanReports(state: String, district: String) {
        firestore.collection("orphan_reports")
            .whereEqualTo("state", state)
            .whereEqualTo("district", district)
            .get()
            .addOnSuccessListener { documents ->
                val reports = documents.mapNotNull { it.toObject(OrphanReport::class.java) }
                orphanReportsAdapter.submitList(reports)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load reports: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
