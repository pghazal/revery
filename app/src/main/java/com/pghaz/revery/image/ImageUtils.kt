package com.pghaz.revery.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun getCoverArtFilePath(context: Context?, uri: Uri?): String {
        if (uri != null && isInternalFile(uri)) {
            val directoryPath: String = context?.externalCacheDir?.path + "/covers"
            val fileName = "${uri.lastPathSegment}.png"
            val filePath = "$directoryPath/$fileName"
            val file = File(filePath)
            return if (file.exists()) {
                "file://" + file.path
            } else {
                val bitmap = getCoverArtBitmap(context, uri)
                "file://" + createBitmapFileAndGetPath(directoryPath, fileName, bitmap)
            }
        }

        return uri.toString()
    }

    fun getSpotifyImageFilePath(context: Context?, stringUri: String?, bitmap: Bitmap?): String {
        if (stringUri != null && isSpotifyFile(stringUri)) {
            val directoryPath: String = context?.externalCacheDir?.path + "/spotify"
            val fileName = "${stringUri}.png"
            val filePath = "$directoryPath/$fileName"
            val file = File(filePath)
            return if (file.exists()) {
                "file://" + file.path
            } else {
                "file://" + createBitmapFileAndGetPath(directoryPath, fileName, bitmap)
            }
        }

        return stringUri.toString()
    }

    fun isInternalFile(uri: Uri?): Boolean {
        return uri?.scheme == "content" || uri?.scheme == "file"
    }

    fun isCoverArtExists(uri: Uri?): Boolean {
        if (isInternalFile(uri)) {
            val filePath: String = uri?.path.toString()
            val file = File(filePath)
            return file.exists()
        }

        return false
    }

    private fun isSpotifyFile(stringUri: String?): Boolean {
        return stringUri?.startsWith("spotify") ?: false
    }

    private fun getCoverArtBitmap(context: Context?, uri: Uri): Bitmap? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(context, uri)
            val data: ByteArray? = mediaMetadataRetriever.embeddedPicture
            if (data != null) BitmapFactory.decodeByteArray(data, 0, data.size) else null
        } catch (ex: Exception) {
            null
        }
    }

    private fun createBitmapFileAndGetPath(
        directoryPath: String,
        fileName: String,
        bitmap: Bitmap?
    ): String {
        val directory = File(directoryPath)
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, fileName)
        if (!file.exists()) {
            file.createNewFile()

            val outputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        }

        return file.path
    }
}