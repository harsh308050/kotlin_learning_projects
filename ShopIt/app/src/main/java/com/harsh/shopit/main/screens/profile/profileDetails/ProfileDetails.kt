package com.harsh.shopit.main.screens.profile.profileDetails

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.profile.data.model.UserResponseModel
import com.harsh.shopit.main.utils.Prefs
import com.harsh.shopit.main.utils.SharedPrefKeys
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileDetails : AppCompatActivity() {
    private val profileImage by lazy {
        findViewById<ShapeableImageView>(R.id.profileImage)
    }
    private var selectedImageUri: String? = null
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri.toString()
                profileImage.setImageURI(uri)
            }
        }

    fun showDatePicker() {
        val dobField = findViewById<EditText>(R.id.dateofbirthProfile)
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectionMillis = if (dobField.text.contains("-")) {
            displayFormat.parse(dobField.text.toString())?.time
        } else {
            apiFormat.parse(dobField.text.toString())?.time
        }
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth").setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setEnd(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            ).setTheme(R.style.AppDatePicker)
            .setSelection(selectionMillis ?: MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(supportFragmentManager, "DOB_PICKER")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formattedDate = displayFormat.format(Date(selection))
            dobField.setText(formattedDate)
        }
    }

    fun profileImageSelection() {
        val editBtn = findViewById<ImageButton>(R.id.editProfileBtn)
        editBtn.setOnClickListener {
            imagePicker.launch("image/*")
        }
    }

    fun getUserData() {
        val user = Prefs.getObject<UserResponseModel>(this, SharedPrefKeys.userDetails)
        val userName = findViewById<TextView>(R.id.userNameProfile)
        val userProfileImage = findViewById<ShapeableImageView>(R.id.profileImage)
        val firstNameField = findViewById<EditText>(R.id.firstNameField)
        val lastNameField = findViewById<EditText>(R.id.lastNameField)
        val emailField = findViewById<EditText>(R.id.emailFieldProfile)
        val phoneField = findViewById<EditText>(R.id.phoneFieldProfile)
        val dobField = findViewById<EditText>(R.id.dateofbirthProfile)

        if (user != null) {
            userName.text = user.firstName + " " + user.lastName
            Glide.with(this).load(user.image).into(userProfileImage)
            firstNameField.setText(user.firstName)
            lastNameField.setText(user.lastName)
            emailField.setText(user.email)
            phoneField.setText(user.phone)
            dobField.setText(user.birthDate)
        }
        dobField.setOnClickListener {
            showDatePicker()
        }
    }

    fun navback(){
        val navback = findViewById<ImageButton>(R.id.navback)
        navback.setOnClickListener { finish() }
    }

    fun saveChanges() {
        profileImageSelection()
        val saveBtn = findViewById<TextView>(R.id.saveChange)
        val firstNameField = findViewById<EditText>(R.id.firstNameField)
        val lastNameField = findViewById<EditText>(R.id.lastNameField)
        val emailField = findViewById<EditText>(R.id.emailFieldProfile)
        val phoneField = findViewById<EditText>(R.id.phoneFieldProfile)
        val dobField = findViewById<EditText>(R.id.dateofbirthProfile)
        saveBtn.setOnClickListener {
            val user = Prefs.getObject<UserResponseModel>(this, SharedPrefKeys.userDetails)
            if (user != null) {
                user.firstName = firstNameField.text.toString()
                user.lastName = lastNameField.text.toString()
                user.email = emailField.text.toString()
                user.phone = phoneField.text.toString()
                user.birthDate = dobField.text.toString()
                user.image = selectedImageUri.toString()
            }

            Prefs.putObject(this, SharedPrefKeys.userDetails, user)
            Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_activity_profile_details)
        getUserData()
        saveChanges()
        navback()
    }
}