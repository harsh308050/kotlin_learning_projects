package com.harsh.worksphere.manager.settings

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.harsh.worksphere.R
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import kotlinx.coroutines.launch

class ManagerProfileActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var phoneField: EditText
    private lateinit var updateBtn: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var navBack: ImageButton

    private val firestoreDataSource = FirestoreDataSource()
    private val currentUserEmail = FirebaseModule.auth.currentUser?.email ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.manager_profile_activity)

        initViews()
        loadUserProfile()
        setupListeners()
    }

    private fun initViews() {
        nameField = findViewById(R.id.manager_name_field)
        emailField = findViewById(R.id.manager_email_field)
        phoneField = findViewById(R.id.manager_number_field)
        updateBtn = findViewById(R.id.manager_profile_update_btn)
        progressBar = findViewById(R.id.profile_progress_bar)
        navBack = findViewById(R.id.navback)
    }

    private fun setupListeners() {
        navBack.setOnClickListener { finish() }
        updateBtn.setOnClickListener { updateProfile() }
    }

    private fun loadUserProfile() {
        if (currentUserEmail.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            when (val result = firestoreDataSource.getUser(currentUserEmail)) {
                is Result.Success -> {
                    val user = result.data
                    if (user != null) {
                        nameField.setText(user.name)
                        emailField.setText(user.email)
                        phoneField.setText(user.phone)
                    } else {
                        Toast.makeText(this@ManagerProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(this@ManagerProfileActivity, result.message, Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
            showLoading(false)
        }
    }

    private fun updateProfile() {
        val name = nameField.text.toString().trim()
        val phone = phoneField.text.toString().trim()

        if (name.isEmpty()) {
            nameField.error = "Name is required"
            nameField.requestFocus()
            return
        }

        if (phone.isNotEmpty() && phone.length != 10) {
            phoneField.error = "Enter a valid 10-digit number"
            phoneField.requestFocus()
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            when (val result = firestoreDataSource.updateUserProfile(currentUserEmail, name, phone)) {
                is Result.Success -> {
                    Toast.makeText(this@ManagerProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this@ManagerProfileActivity, result.message, Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            updateBtn.text = ""
            updateBtn.isEnabled = false
            progressBar.visibility = View.VISIBLE
        } else {
            updateBtn.text = "Update Profile"
            updateBtn.isEnabled = true
            progressBar.visibility = View.GONE
        }
    }
}