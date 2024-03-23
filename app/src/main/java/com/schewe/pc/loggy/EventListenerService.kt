package com.schewe.pc.loggy

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.google.android.gms.wearable.Wearable
import com.schewe.pc.loggy.api.LogService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        Log.d("X", "Data changed")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path?.startsWith("/path/to/audio/data") == true
            ) {

                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val audioData = dataMapItem.dataMap.getByteArray("audio_data")
                val timestamp = dataMapItem.dataMap.getLong("timestamp")

                Log.d("X", "Collected audio data")

                // Hier können Sie die Audiodaten weiterverarbeiten
                writeAudioDataToFile(audioData!!, timestamp)
                uploadFiles()
            }
        }
    }

    companion object{
        private const val FILE_SIZE_LIMIT = 1 * 1024 * 1024  // 1 MB
    }

    private fun writeAudioDataToFile(audioData: ByteArray, timestamp: Long) {
        try {
            val streamFile = File(filesDir, "stream.pcm")
            val metadataFile = File(filesDir, "metadata.txt")

            // Erstellt die Datei, wenn sie nicht existiert
            if (!streamFile.exists()) {
                streamFile.createNewFile()
                metadataFile.createNewFile()
                metadataFile.writeText("$timestamp")
            }

            val outputStream = FileOutputStream(streamFile, true)
            outputStream.write(audioData)

            Log.d("X", "Wrote to file.")

            if (streamFile.length() > FILE_SIZE_LIMIT) {
                Log.d("X", "New file because file already too big: ${streamFile.length()}")

                // Schließt den aktuellen Stream und startet einen neuen für die nächste Datei
                outputStream.close()

                val beginTimestamp = metadataFile.readText()
                val uploadFile = File(filesDir, "$beginTimestamp-upload.pcm")
                streamFile.renameTo(uploadFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun uploadFiles() {
        val directory = File(filesDir, "/")
        val files = directory.listFiles { _, name -> name.endsWith("upload.pcm") }

        files?.forEach { file ->
            Log.d("X", "File found for upload ${file.name}.")
            if(file.length() > FILE_SIZE_LIMIT) {
                val timestamp = file.name.split("-")[0].toLong()
                try {
                    LogService.createAudio(file, timestamp)

                    Log.d("X", "File was uploaded.")

                    // Löscht die Datei, nachdem sie hochgeladen wurde
                    file.delete()
                } catch (e: Exception) {
                    Log.d("X", "File could not be uploaded: ${e.message}")
                }
            }
        }
    }
}