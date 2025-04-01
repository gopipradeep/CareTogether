package com.example.orphans

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.orphans.databinding.ActivityDonorBinding

class DonorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(OrphanagesFragment())
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_orphanages -> {
                    loadFragment(OrphanagesFragment())
                    true
                }
                R.id.navigation_slum_areas -> {
                    loadFragment(SlumAreasFragment())
                    true
                }
                R.id.navigation_report_orphan -> {
                    loadFragment(ReportOrphanFragment())
                    true
                }
                R.id.navigation_feedback -> {
                    loadFragment(FeedbackFragment())
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
