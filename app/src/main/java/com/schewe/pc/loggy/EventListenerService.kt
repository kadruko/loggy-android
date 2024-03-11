package com.schewe.pc.loggy

import android.util.Log
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.schewe.pc.loggy.api.LogService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EventListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("event-listener-service", "Data changed: ${dataEvents.count}")
        dataEvents.forEach { event ->
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    Log.d("event-listener-service", "Event received: ${event.dataItem.uri.path}")
                    val path = event.dataItem.uri.path
                    if (path.equals("/audio")) {
                        DataMapItem.fromDataItem(event.dataItem).dataMap.apply {
                            val audioAsset = getAsset("audioAsset")
                            if (audioAsset != null) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val assetFile = createFileFromAsset(audioAsset)
                                    LogService.createAudio(assetFile)
                                    assetFile.delete()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun createFileFromAsset(asset: Asset): File {
        return withContext(Dispatchers.IO) {
            val file = File(filesDir, generateFileName())
            val assetInputStream = Wearable.getDataClient(applicationContext).getFdForAsset(asset).result.inputStream
            val fileOutputStream = FileOutputStream(file)
            assetInputStream.copyTo(fileOutputStream)
            file
        }
    }

    private fun generateFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "loggy_$timestamp.pcm"
    }
}