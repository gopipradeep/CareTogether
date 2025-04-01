package com.example.orphans

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.orphans.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)


        initializeFirebase()


        configureGoogleSignIn()

        setClickListeners()
    }

    private fun initializeFirebase() {
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setClickListeners() {
        binding.logIn.setOnClickListener { showLogInLayout() }
        binding.singUp.setOnClickListener { showSignUpLayout() }
        binding.signIn.setOnClickListener { handleSignIn() }
        binding.signUpButton.setOnClickListener { handleSignUp() }
        binding.signInGoogle.setOnClickListener { signInWithGoogle() }
    }

    private fun showLogInLayout() {
        binding.logInLayout.visibility = View.VISIBLE
        binding.signUpLayout.visibility = View.GONE
        binding.logIn.setTextColor(ContextCompat.getColor(this, R.color.textColor))
        binding.singUp.setTextColor(ContextCompat.getColor(this, R.color.pinkColor))
        binding.logIn.background = ContextCompat.getDrawable(this, R.drawable.switch_trcks)
        binding.singUp.background = null
    }

    private fun showSignUpLayout() {
        binding.logInLayout.visibility = View.GONE
        binding.signUpLayout.visibility = View.VISIBLE
        binding.logIn.setTextColor(ContextCompat.getColor(this, R.color.pinkColor))
        binding.singUp.setTextColor(ContextCompat.getColor(this, R.color.textColor))
        binding.singUp.background = ContextCompat.getDrawable(this, R.drawable.switch_trcks)
        binding.logIn.background = null
    }

    private fun handleSignIn() {
        val email = binding.eMail.text.toString().trim()
        val password = binding.passwords.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkUserRoleAndRedirect(email,password)
                } else {
                    Log.e("AuthActivity", "Sign In failed: ${task.exception?.message}")
                    Toast.makeText(this, "Log In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleSignUp() {
        val email = binding.eMails.text.toString().trim()
        val password = binding.passwordss.text.toString().trim()
        val confirmPassword = binding.passwordsConfirm.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    saveUserDetails(userId)
                } else {
                    Log.e("AuthActivity", "Sign Up failed: ${task.exception?.message}")
                    Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDetails(userId: String) {
        val userMap = hashMapOf(
            "isNewUser" to true,
            "role" to "default"
        )

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                navigateToDetailsScreen()
            }
            .addOnFailureListener { e ->
                Log.e("AuthActivity", "Error saving user: ${e.message}")
                Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserRoleAndRedirect(email: String, password: String) {


        if (email == "admin@gmail.com" && password == "123456") {
            navigateToAdminActivity()
            return
        }

        val userId = auth.currentUser?.uid ?: return


        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    when (role) {
                        "Donor" -> navigateToDonorActivity()
                        "Organization" -> navigateToOrganizationActivity()
                        else -> navigateToDetailsScreen()
                    }
                } else {
                    navigateToDetailsScreen()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthActivity", "Failed to get user role: ${e.message}")
                navigateToDetailsScreen()
            }
    }

    private fun navigateToDetailsScreen() {
        startActivity(Intent(this, DetailsActivity::class.java))
        finish()
    }

    private fun navigateToDonorActivity() {
        startActivity(Intent(this, DonorActivity::class.java))
        finish()
    }

    private fun navigateToOrganizationActivity() {
        startActivity(Intent(this, OrganizationActivity::class.java))
        finish()
    }
    private fun navigateToAdminActivity() {
        startActivity(Intent(this, AdminActivity::class.java))
        finish()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.e("AuthActivity", "Google Sign-In failed: ${e.message}")
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        checkUserRoleAndRedirect(currentUser.email ?: "", "123456")
                    }
                } else {
                    Log.e("AuthActivity", "Firebase Authentication failed: ${task.exception?.message}")
                    Toast.makeText(this, "Firebase Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
