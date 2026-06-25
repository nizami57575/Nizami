package com.example.ui.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun copyResourceToCache(context: Context, resId: Int, fileName: String): Uri? {
        return try {
            val cacheFile = File(context.cacheDir, fileName)
            if (!cacheFile.exists()) {
                val inputStream = context.resources.openRawResource(resId)
                val outputStream = FileOutputStream(cacheFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }
            Uri.fromFile(cacheFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
