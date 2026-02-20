package com.harsh.worksphere.employee.visitlogs

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.harsh.worksphere.R
import com.harsh.worksphere.components.CommonSnackbar.showError
import com.harsh.worksphere.components.CommonSnackbar.showSuccessAndFinish
import com.harsh.worksphere.core.firebase.FirebaseModule
import com.harsh.worksphere.core.utils.CloudinaryHelper
import com.harsh.worksphere.initial.auth.data.remote.FirestoreDataSource
import com.harsh.worksphere.manager.sites.data.model.SiteModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmployeeAddVisitLogActivity : AppCompatActivity() {

    private lateinit var navback: ImageButton
    private lateinit var siteNameField: EditText
    private lateinit var visitDateTimeField: EditText
    private lateinit var visitNotesField: EditText
    private lateinit var addEvidenceBtn: FrameLayout
    private lateinit var evidenceRecyclerView: RecyclerView
    private lateinit var submitBtn: MaterialButton
    private lateinit var loader: ProgressBar

    private var site: SiteModel? = null
    private val evidenceImages = mutableListOf<Uri>()
    private lateinit var evidenceAdapter: EvidenceImageAdapter

    private var currentPhotoUri: Uri? = null

    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            showError("Camera permission is required to take photos")
        }
    }

    // Camera launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                evidenceImages.add(uri)
                evidenceAdapter.notifyItemInserted(evidenceImages.size - 1)
                updateEvidenceVisibility()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.employee_add_visit_log_activity)

        @Suppress("DEPRECATION")
        site = intent.getParcelableExtra("site")

        initViews()
        setupEvidenceRecycler()
        populateFields()
        setupListeners()
    }

    private fun initViews() {
        navback = findViewById(R.id.navback)
        siteNameField = findViewById(R.id.employee_current_site_name)
        visitDateTimeField = findViewById(R.id.visit_date_time_field)
        visitNotesField = findViewById(R.id.site_work_details_field)
        addEvidenceBtn = findViewById(R.id.add_evidence_btn)
        evidenceRecyclerView = findViewById(R.id.evidence_preview_images)
        submitBtn = findViewById(R.id.employee_add_visit_log_btn)
        loader = findViewById(R.id.employee_add_visit_log_loader)
    }

    private fun setupEvidenceRecycler() {
        evidenceAdapter = EvidenceImageAdapter(evidenceImages) { position ->
            evidenceImages.removeAt(position)
            evidenceAdapter.notifyItemRemoved(position)
            evidenceAdapter.notifyItemRangeChanged(position, evidenceImages.size)
            updateEvidenceVisibility()
        }
        evidenceRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        evidenceRecyclerView.adapter = evidenceAdapter
    }

    private fun populateFields() {
        val s = site
        if (s != null) {
            siteNameField.setText(s.siteName)
        }

        // Format: "Feb 20, 2026 - 07:00 PM"
        val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        visitDateTimeField.setText(dateFormat.format(Date()))
    }

    private fun setupListeners() {
        navback.setOnClickListener { finish() }

        addEvidenceBtn.setOnClickListener {
            if (evidenceImages.size >= 5) {
                showError("Maximum 5 images allowed")
                return@setOnClickListener
            }
            if (hasCameraPermission()) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        submitBtn.setOnClickListener { validateAndSubmit() }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun launchCamera() {
        val photoFile = File(cacheDir, "evidence_${System.currentTimeMillis()}.jpg")
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(currentPhotoUri!!)
    }

    private fun updateEvidenceVisibility() {
        evidenceRecyclerView.visibility =
            if (evidenceImages.isEmpty()) View.GONE else View.VISIBLE
    }

    // ── Validation & Submit ──────────────────────────────────────────────────
    private fun validateAndSubmit() {
        val notes = visitNotesField.text.toString().trim()
        if (notes.isEmpty()) {
            showError("Please enter visit notes")
            return
        }
        if (evidenceImages.isEmpty()) {
            showError("Please attach at least one evidence photo")
            return
        }
        val s = site
        if (s == null) {
            showError("Site information is missing")
            return
        }

        submitBtn.isEnabled = false
        submitBtn.text = ""
        loader.visibility = View.VISIBLE


        lifecycleScope.launch {
            try {
                // Upload images to Cloudinary
                val imageUrls = mutableListOf<String>()
                for (uri in evidenceImages) {
                    val url = CloudinaryHelper.uploadImage(this@EmployeeAddVisitLogActivity, uri, "visit_evidence")
                    if (url != null) {
                        imageUrls.add(url)
                    }
                }

                if (imageUrls.isEmpty()) {
                    showError("Failed to upload evidence images")
                    submitBtn.isEnabled = true
                    loader.visibility = View.GONE
                    submitBtn.text = "Add Visit Log"

                    return@launch
                }

                val userId = FirebaseModule.auth.currentUser?.email ?: return@launch
                val firestore = FirebaseModule.firestore

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.format(Date())
                val displayDateFormat = SimpleDateFormat("dd MMM, yyyy - h:mm a", Locale.getDefault())

                // Create unified visit log record
                val recordTimestamp = System.currentTimeMillis()
                val recordId = "record_$recordTimestamp"
                val formattedTimestamp = displayDateFormat.format(Date(recordTimestamp))

                // Get supervisor ID from the current user
                val supervisorId = try {
                    val userResult = FirestoreDataSource().getUser(userId)
                    if (userResult is com.harsh.worksphere.core.utils.Result.Success) {
                        userResult.data?.mySupervisor
                    } else null
                } catch (_: Exception) { null }

                val visitLogRecord = hashMapOf(
                    "siteId" to s.siteId,
                    "siteName" to s.siteName,
                    "siteAddress" to s.location.address,
                    "siteLocation" to mapOf(
                        "latitude" to s.location.latitude,
                        "longitude" to s.location.longitude
                    ),
                    "visitNotes" to notes,
                    "evidenceImages" to imageUrls,
                    "timestamp" to formattedTimestamp,
                    "timestampMillis" to recordTimestamp,
                    "status" to "ON_SITE",
                    "userId" to userId,
                    "supervisorId" to supervisorId
                )

                // Store in visitLogs/{userId}/{date}/record_{timestampMillis}
                firestore.collection("visitLogs")
                    .document(userId)
                    .collection(date)
                    .document(recordId)
                    .set(visitLogRecord)
                    .await()

                setResult(RESULT_OK)
                showSuccessAndFinish("Visit log added successfully!")

            } catch (e: Exception) {
                showError("Failed to submit: ${e.localizedMessage}")
                submitBtn.isEnabled = true
                loader.visibility = View.GONE
                submitBtn.text = "Add Visit Log"
            }
        }
    }
}