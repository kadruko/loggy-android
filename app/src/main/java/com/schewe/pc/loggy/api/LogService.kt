package com.schewe.pc.loggy.api

import java.io.File
import java.net.URL

class LogService {
    companion object {
        val baseURL = "http://89.58.29.139:3020";

        fun createAudio(file: File) {
            val url = URL("${baseURL}/audio")
            val multipart = Multipart(url)
            multipart.addFilePart("audio", file, file.name, "audio/pcm")
            multipart.upload(onFileUploadedListener = null)
        }
    }
}