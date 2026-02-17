package com.harsh.noteitform.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.harsh.noteitform.R

class CommonSignature @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private lateinit var fieldHeader: CommonHeader
    private lateinit var signatureCanvas: SignatureCanvas
    private lateinit var clearButton: ImageButton
    private lateinit var drawButton: MaterialButton
    private lateinit var uploadButton: MaterialButton
    private lateinit var uploadedImageView: ImageView
    private lateinit var hintText: TextView
    private lateinit var buttonContainer: LinearLayout
    private lateinit var signaturePad: RelativeLayout

    private var isDrawMode = true
    private var isExpanded = true
    private var galleryLauncher: ActivityResultLauncher<Intent>? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.common_signature, this, true)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        fieldHeader = findViewById(R.id.signatureHeader)
        signatureCanvas = findViewById(R.id.signatureCanvas)
        clearButton = findViewById(R.id.btnClearSignature)
        drawButton = findViewById(R.id.drawSign)
        uploadButton = findViewById(R.id.uploadImage)
        uploadedImageView = findViewById(R.id.uploadedSign)
        hintText = findViewById(R.id.signHint)

        buttonContainer = findViewById(R.id.btnContainer)
        signaturePad = findViewById(R.id.signaturePad)
    }

    private fun setupListeners() {
        signatureCanvas.setOnTouchListener { v, event ->
            val handled = v.onTouchEvent(event)
            if (!signatureCanvas.isEmpty()) {
                hintText.visibility = View.INVISIBLE
            }
            handled
        }

        drawButton.setOnClickListener {
            if (!isDrawMode) {
                isDrawMode = true
                updateButtonStyles()
                updateVisibility()
            }
        }

        uploadButton.setOnClickListener {
            signatureCanvas.clear()
            if (isDrawMode) {
                isDrawMode = false
                updateButtonStyles()
                updateVisibility()
            }
            openGallery()
        }

        uploadedImageView.setOnClickListener {
            openGallery()
        }

        clearButton.setOnClickListener {
            if (isDrawMode) {
                signatureCanvas.clear()
                hintText.text = "Draw Signature"
                hintText.visibility = View.VISIBLE
            } else {
                uploadedImageView.setImageDrawable(null)
                hintText.text = "Upload an Image"
                hintText.visibility = View.VISIBLE
            }
        }
    }
    private var isRequiredField: Boolean = false
    fun config(
        title: String = "",
        isRequired: Boolean = false,
        showInfo: Boolean = true,
        infoTooltip: String = "",
        showHeader: Boolean = true,
        isNested: Boolean = false,
        isExpandable: Boolean = false,
        isInitiallyExpanded: Boolean = true
    ) {
        this.isExpanded = isInitiallyExpanded
        this.isRequiredField = isRequired
        if (showHeader) {
            fieldHeader.visibility = View.VISIBLE
            val backgroundColor = if (isNested) {
                R.color.light_grey
            } else {
                null
            }
            fieldHeader.setup(
                config = CommonHeader.Config(
                    title = title,
                    isRequired = isRequired,
                    showInfoButton = showInfo,
                    showAddButton = false,
                    backgroundColorRes = backgroundColor,
                    showExpandButton = isExpandable,
                    isInitiallyExpanded = isInitiallyExpanded,
                    infoTooltip = infoTooltip
                ),
                listener = if (isExpandable) object : CommonHeader.OnHeaderActionListener {
                    override fun onExpandClick(expanded: Boolean) {
                        isExpanded = expanded
                        animateToggleSignature()
                    }
                } else null
            )
        } else {
            fieldHeader.visibility = View.GONE
        }


        updateButtonStyles()
        updateVisibility()
        toggleContent()
    }
    fun isRequired(): Boolean = isRequiredField
    private fun toggleContent() {
        val contentVisibility = if (isExpanded) View.VISIBLE else View.GONE
        buttonContainer.visibility = contentVisibility
        signaturePad.visibility = contentVisibility
    }

    private fun animateToggleSignature() {
        if (isExpanded) {
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    buttonContainer.visibility =  View.VISIBLE
                    signaturePad?.visibility  = View.VISIBLE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            buttonContainer.startAnimation(anim)
            signaturePad.startAnimation(anim)
        }
        else{
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    buttonContainer.visibility =  View.GONE
                    signaturePad?.visibility  = View.GONE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            buttonContainer.startAnimation(anim)
            signaturePad.startAnimation(anim)
        }
    }
    private var onGalleryLaunch: ((CommonSignature) -> Unit)? = null
    fun setGalleryLauncher(
        gallery: ActivityResultLauncher<Intent>,
        onLaunch: (CommonSignature) -> Unit
    ) {
        this.galleryLauncher = gallery
        this.onGalleryLaunch = onLaunch
    }

    private fun openGallery() {
        onGalleryLaunch?.invoke(this)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher?.launch(intent)
    }

    fun handleGalleryResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let { uri ->
                setUploadedImage(uri)
                hintText.visibility = View.INVISIBLE

                if (isDrawMode) {
                    isDrawMode = false
                    updateButtonStyles()
                    updateVisibility()
                }
            }
        }
    }

    private fun updateButtonStyles() {
        if (isDrawMode) {
            drawButton.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.dark_grey)
            drawButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            uploadButton.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.light_grey)
            uploadButton.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else {
            drawButton.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.light_grey)
            drawButton.setTextColor(ContextCompat.getColor(context, R.color.black))
            uploadButton.setTextColor(ContextCompat.getColor(context, R.color.white))
            uploadButton.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.dark_grey)
        }
    }

    private fun updateVisibility() {
        if (isDrawMode) {
            signatureCanvas.visibility = View.VISIBLE
            uploadedImageView.visibility = View.GONE

            if (signatureCanvas.isEmpty()) {
                hintText.text = "Draw Signature"
                hintText.visibility = View.VISIBLE
            } else {
                hintText.visibility = View.INVISIBLE
            }
        } else {
            signatureCanvas.visibility = View.GONE
            uploadedImageView.visibility = View.VISIBLE

            if (uploadedImageView.drawable == null) {
                hintText.text = "Upload an Image"
                hintText.visibility = View.VISIBLE
            } else {
                hintText.visibility = View.INVISIBLE
            }
        }
    }

    fun setUploadedImage(uri: Uri) {
        uploadedImageView.setImageURI(uri)
        hintText.visibility = View.INVISIBLE
    }



    fun getSignatureBitmap(): Bitmap? {
        return if (isDrawMode) {
            if (!signatureCanvas.isEmpty()) {
                signatureCanvas.getSignatureBitmap()
            } else null
        } else {
            uploadedImageView.drawable?.let { drawable ->
                val width = drawable.intrinsicWidth.coerceAtLeast(1)
                val height = drawable.intrinsicHeight.coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }
    }

}