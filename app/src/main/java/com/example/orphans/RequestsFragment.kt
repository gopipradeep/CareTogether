package com.example.orphans

import Need
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class RequestsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var needsRecyclerView: RecyclerView
    private lateinit var needEditText: EditText
    private lateinit var submitNeedButton: Button
    private lateinit var noNeedsTextView: TextView
    private lateinit var needsAdapter: NeedsAdapter
    private val needsList = mutableListOf<Need>()

    private lateinit var organizationName: String
    private lateinit var contactNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        needsRecyclerView = view.findViewById(R.id.needsRecyclerView)
        needEditText = view.findViewById(R.id.needEditText)
        submitNeedButton = view.findViewById(R.id.submitNeedButton)
        noNeedsTextView = view.findViewById(R.id.noNeedsTextView)

        fetchOrganizationName()

        needsAdapter = NeedsAdapter(needsList) { need -> deleteNeed(need) }
        needsRecyclerView.adapter = needsAdapter
        needsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        submitNeedButton.setOnClickListener {
            val needDescription = needEditText.text.toString().trim()
            if (needDescription.isNotEmpty()) {
                submitNeed(needDescription)
            }
        }

        fetchNeeds()
    }

    private fun fetchOrganizationName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        organizationName = document.getString("name") ?: "Unknown Organization"
                        contactNumber=document.getString("contactNumber")?:"Unknown"

                    } else {
                        organizationName = "Unknown Organization"
                        contactNumber="Unknown"
                    }
                }
                .addOnFailureListener { exception ->
                    organizationName = "Unknown Organization"
                    contactNumber="Unknown"
                }
        } else {
            organizationName = "Unknown Organization"
        }
    }

    private fun submitNeed(description: String) {
        val need = Need(description = description, organizationname = organizationName,contactNumber=contactNumber )
        firestore.collection("needs")
            .add(need)
            .addOnSuccessListener {
                needEditText.text.clear()
                fetchNeeds()
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun fetchNeeds() {
        firestore.collection("needs")
            .get()
            .addOnSuccessListener { documents ->
                needsList.clear()
                for (document in documents) {
                    val need = document.toObject(Need::class.java).copy(id = document.id)
                    needsList.add(need)
                }
                needsAdapter.notifyDataSetChanged()

                if (needsList.isEmpty()) {
                    noNeedsTextView.visibility = View.VISIBLE
                    needsRecyclerView.visibility = View.GONE
                } else {
                    noNeedsTextView.visibility = View.GONE
                    needsRecyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun deleteNeed(need: Need) {
        firestore.collection("needs").document(need.id)
            .delete()
            .addOnSuccessListener {
                fetchNeeds()
            }
            .addOnFailureListener { exception ->
            }
    }
}
