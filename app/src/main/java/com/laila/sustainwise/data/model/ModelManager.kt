package com.laila.sustainwise.data.model

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import java.io.File

class ModelManager(private val context: Context) {

    private val fileName = "model_rekomendasi.tflite"
    private val modelUrl = "https://storage.googleapis.com/modelai_sustainwise/model_rekomendasi.tflite"

    fun getModelFile(): File {
        return File(context.getExternalFilesDir(null), fileName)
    }

    fun isModelAvailable(): Boolean {
        return getModelFile().exists()
    }

    fun downloadModel() {
        if (isModelAvailable()) {
            Toast.makeText(context, "Model already exists: ${getModelFile().absolutePath}", Toast.LENGTH_SHORT).show()
            return
        }

        val destinationFile = getModelFile()
        val request = DownloadManager.Request(Uri.parse(modelUrl))
            .setTitle("Downloading Model")
            .setDescription("Downloading recommendation model")
            .setDestinationUri(Uri.fromFile(destinationFile))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        Toast.makeText(context, "Model download started", Toast.LENGTH_SHORT).show()
    }
}