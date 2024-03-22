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
            }
        }
    }

    companion object{
        private const val FILE_SIZE_LIMIT = 1 * 1024 * 1024  // 1 MB
    }

    private var outputStream: FileOutputStream? = null
    private var file: File? = null

    private fun writeAudioDataToFile(audioData: ByteArray) {
        try {
            if (outputStream == null) {
                file = File("audio.pcm")

                // Erstellt die Datei, wenn sie nicht existiert
                if (!file!!.exists()) {
                    file!!.createNewFile()
                }

                outputStream = FileOutputStream(file!!)
            }

            outputStream!!.write(audioData)

            if (file!!.length() > FILE_SIZE_LIMIT) {
                try {
                    LogService.createAudio(file!!)

                    // Schließt den aktuellen Stream und startet einen neuen für die nächste Datei
                    outputStream!!.close()
                    outputStream = null
                    file!!.delete()
                    file = null
                }catch (e: Exception){
                    Log.d("X", "File could not be uploaded.")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}