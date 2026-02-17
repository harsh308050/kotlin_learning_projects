package com.harsh.worksphere.core.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileUtils {

    fun copyImageToAppStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val dir = File(context.filesDir, "site_images")
            if (!dir.exists()) dir.mkdirs()

            val fileName = "site_${UUID.randomUUID()}.jpg"
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