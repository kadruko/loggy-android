package com.schewe.pc.loggy.api

import android.util.Log
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter


class LogService {
    companion object {
        val baseURL = "http://89.58.29.139:3021";

        fun createAudio(file: File, timestamp: Long) {
            val instant = Instant.ofEpochMilli(timestamp)
            val timestampIso = DateTimeFormatter.ISO_INSTANT.format(instant)
            val url = "${baseURL}/audios"

            Log.d("X", "Prepare file upload with file ${file.name}, timestamp $timestampIso")

            val httpClient: HttpClient = DefaultHttpClient()
            val httpPost = HttpPost(url)
            val mpEntity = MultipartEntity()
            val cbFile: ContentBody = FileBody(file, "audio/pcm")
            mpEntity.addPart("audio", cbFile)
            mpEntity.addPart("timestamp", StringBody(timestampIso))
            httpPost.entity = mpEntity
            val response: HttpResponse = httpClient.execute(httpPost)
            val resEntity = response.entity
            val result = resEntity.toString()

            if(response.statusLine.statusCode != HttpStatus.SC_CREATED){
                throw Exception("Upload error: $result")
            }
        }
    }
}