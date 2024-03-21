package com.schewe.pc.loggy

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

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
            }
        }
    }
}