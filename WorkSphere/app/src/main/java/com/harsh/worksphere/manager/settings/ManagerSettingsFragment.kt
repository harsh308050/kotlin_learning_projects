package com.harsh.worksphere.manager.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.harsh.worksphere.R
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.core.utils.FileUtils
import com.harsh.worksphere.core.utils.Result
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import com.harsh.worksphere.initial.auth.ui.LoginActivity
import com.harsh.worksphere.manager.notifications.ManagerNotificationActivity
import kotlinx.coroutines.launch

class ManagerSettingsFragment : Fragment(R.layout.manager_settings_fragment) {
    private lateinit var profileSettingsBtn: LinearLayout
    private lateinit var notificationSettingsBtn: LinearLayout
    private lateinit var logout: MaterialButton
    private lateinit var profileImage: ImageView
    private lateinit var profileEditIcon: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileRole: TextView
    private lateinit var profileEmail: TextView


    private val firestoreDataSource = FirestoreDataSource()
    private val currentUserEmail = FirebaseModule.auth.currentUser?.email ?: ""

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadUserProfile()
        setupImagePicker()
        navToProfile()
        navToNotification()
        logout()
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun initViews() {
        profileSettingsBtn = requireView().findViewById(R.id.manager_profile_settings_tab)
        notificationSettingsBtn = requireView().findViewById(R.id.manager_notification_settings_tab)
        logout = requireView().findViewById(R.id.manager_logout)
        profileImage = requireView().findViewById(R.id.manager_profile_image)
        profileEditIcon = requireView().findViewById(R.id.manager_profile_edit_icon)
        profileName = requireView().findViewById(R.id.manager_profile_name)
        profileRole = requireView().findViewById(R.id.manager_profile_role)
        profileEmail = requireView().findViewById(R.id.manager_profile_email)
    }

    private fun loadUserProfile() {
        if (currentUserEmail.isEmpty()) return

        lifecycleScope.launch {
            when (val result = firestoreDataSource.getUser(currentUserEmail)) {
                is Result.Success -> {
                    val user = result.data ?: return@launch
                    profileName.text = user.name
                    profileEmail.text = user.email
                    profileRole.text = user.role.value.replaceFirstChar { it.uppercase() }

                    // Load profile image
                    if (!user.profilePic.isNullOrEmpty()) {
                        Glide.with(this@ManagerSettingsFragment)
                            .load(user.profilePic)
                            .placeholder(R.drawable.siteeee)
                            .into(profileImage)
                    }
                }
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
    }

    private fun setupImagePicker() {
        profileEditIcon.setOnClickListener { openGallery() }
        profileImage.setOnClickListener { openGallery() }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun handleSelectedImage(uri: Uri) {
        val persistentPath = FileUtils.copyImageToAppStorage(requireContext(), uri, "profile_images", "profile")

        if (persistentPath != null) {
            // Show selected image immediately
            Glide.with(this)
                .load(persistentPath)
                .placeholder(R.drawable.siteeee)
                .into(profileImage)

            // Save to Firestore
            lifecycleScope.launch {
                when (val result = firestoreDataSource.updateProfilePic(currentUserEmail, persistentPath)) {
                    is Result.Success -> {
                        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Error -> {
                        Toast.makeText(requireContext(), "Failed to save: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Loading -> {}
                }
            }
        } else {
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navToProfile() {
        profileSettingsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ManagerProfileActivity::class.java))
        }
    }

    private fun navToNotification() {
        notificationSettingsBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ManagerNotificationActivity::class.java))
        }
    }

    private fun logout() {
        logout.setOnClickListener {
            lifecycleScope.launch {
                try {
                    FirebaseModule.auth.signOut()

                    val credentialManager = CredentialManager.create(requireContext())
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())

                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}