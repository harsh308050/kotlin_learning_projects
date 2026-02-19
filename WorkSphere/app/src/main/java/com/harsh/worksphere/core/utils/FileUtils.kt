package com.harsh.worksphere.core.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileUtils {

    fun copyImageToAppStorage(context: Context, sourceUri: Uri, folder: String = "site_images", prefix: String = "site"): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val dir = File(context.filesDir, folder)
            if (!dir.exists()) dir.mkdirs()

            val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
            val destFile = File(dir, fileName)

            inputStream?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}