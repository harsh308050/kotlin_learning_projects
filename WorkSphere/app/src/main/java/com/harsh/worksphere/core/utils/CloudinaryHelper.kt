package com.harsh.worksphere.core.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object CloudinaryHelper {

    private const val CLOUD_NAME = "debf09qz0"
    private const val UPLOAD_PRESET = "workshpere"

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val config = mapOf("cloud_name" to CLOUD_NAME)
        MediaManager.init(context.applicationContext, config)
        isInitialized = true
    }

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String
    ): String? {
        init(context)

        return suspendCoroutine { continuation ->
            MediaManager.get()
                .upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .option("folder", folder)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        continuation.resume(secureUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(null)
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resume(null)
                    }
                })
                .dispatch()
        }
    }
}
