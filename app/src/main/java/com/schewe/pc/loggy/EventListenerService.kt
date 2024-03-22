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

                Log.d("X", "Collected audio data")

                // Hier können Sie die Audiodaten weiterverarbeiten
                writeAudioDataToFile(audioData!!)
                uploadFiles()
            }
        }
    }

    companion object{
        private const val FILE_SIZE_LIMIT = 1 * 1024 * 1024  // 1 MB
        private const val FILE_PATH = "/"
    }

    private var outputStream: FileOutputStream? = null
    private var file: File? = null

    private fun writeAudioDataToFile(audioData: ByteArray) {
        try {
            if (outputStream == null) {
                val timeStamp: Long = Date().time
                file = File(FILE_PATH, "$timeStamp.pcm")

                // Erstellt die Datei, wenn sie nicht existiert
                if (!file!!.exists()) {
                    file!!.createNewFile()
                }

                outputStream = FileOutputStream(file!!)
            }

            outputStream!!.write(audioData)

            if (file!!.length() > FILE_SIZE_LIMIT) {

                // Schließt den aktuellen Stream und startet einen neuen für die nächste Datei
                outputStream!!.close()
                outputStream = null
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun uploadFiles() {
        val directory = File(FILE_PATH)
        val files = directory.listFiles { _, name -> name.endsWith(".pcm") }

        files?.forEach { file ->
            if(file.length() > FILE_SIZE_LIMIT) {
                try {
                    LogService.createAudio(file)

                    // Löscht die Datei, nachdem sie hochgeladen wurde
                    file.delete()
                } catch (e: Exception) {
                    Log.d("X", "File could not be uploaded.")
                }
            }
        }
    }
}