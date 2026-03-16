package com.fruits.settings

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageUtils {
    fun createTempImageUri(context: Context, bytes: ByteArray, prefix: String = "img"): Uri {
        val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)
        return Uri.fromFile(file)
    }
}

