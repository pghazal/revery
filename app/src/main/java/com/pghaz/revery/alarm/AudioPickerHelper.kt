package com.pghaz.revery.alarm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.pghaz.revery.R
import com.pghaz.revery.ringtone.RingtonePickerDialog
import com.pghaz.revery.ringtone.RingtonePickerListener
import java.io.File
import java.io.FileOutputStream

object AudioPickerHelper {

    fun showRingtonePicker(
        context: Context?,
        fragmentManager: FragmentManager,
        listener: RingtonePickerListener
    ) {
        if (context == null) {
            return
        }

        //val defaultUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val ringtonePickerBuilder = RingtonePickerDialog.Builder(context, fragmentManager)
            .setTitle(context.getString(R.string.ringtone_picker_default_title))
            //.setCurrentRingtoneUri(defaultUri)
            .displayDefaultRingtone(true)
            .displaySilentRingtone(true)
            .setPositiveButtonText(context.getString(R.string.select))
            .setCancelButtonText(context.getString(R.string.close))
            .setPlaySampleWhileSelection(true)
            .setListener(listener)

        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE)

        ringtonePickerBuilder.show()
    }

    fun startRingtonePickerForResult(fragment: Fragment, requestCode: Int) {
        val intent = buildRingtonePickerIntent()
        fragment.startActivityForResult(intent, requestCode)
    }

    fun startRingtonePickerForResult(activity: Activity, requestCode: Int) {
        val intent = buildRingtonePickerIntent()
        activity.startActivityForResult(intent, requestCode)
    }

    private fun buildRingtonePickerIntent(): Intent {
        val defaultUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultUri)

        return intent
    }

    fun startMusicPickerForResult(fragment: Fragment, requestCode: Int) {
        val intent = buildMusicPickerIntent()
        fragment.startActivityForResult(intent, requestCode)
    }

    fun startMusicPickerForResult(activity: Activity, requestCode: Int) {
        val intent = buildMusicPickerIntent()
        activity.startActivityForResult(intent, requestCode)
    }

    private fun buildMusicPickerIntent(): Intent {
        val audioIntent = Intent()
        audioIntent.type = "audio/*"
        audioIntent.action = Intent.ACTION_OPEN_DOCUMENT
        audioIntent.addCategory(Intent.CATEGORY_OPENABLE)
        return audioIntent
    }

    fun grantPermissionForUri(context: Context?, data: Intent?) {
        data?.data?.let {
            val uri = it

            context?.grantUriPermission(
                context.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val takeFlags: Int =
                data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
            context?.contentResolver?.takePersistableUriPermission(uri, takeFlags)
        }
    }

    fun getTitle(context: Context?, uri: Uri?): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, uri)
        return if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null) {
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        } else {
            RingtoneManager.getRingtone(context, uri)?.getTitle(context)
                ?.substringBeforeLast(".")
        }
    }

    fun getCoverArtFilePath(context: Context?, uri: Uri?): String {
        if (uri?.scheme == "content" || uri?.scheme == "file") {
            val filePath: String =
                context?.externalCacheDir?.path + "/covers/" + "${uri.lastPathSegment}.png"
            val file = File(filePath)
            return if (file.exists()) {
                "file://" + file.path
            } else {
                val bitmap = getCoverArtBitmap(context, uri)
                "file://" + createBitmapFileAndGetPath(context, uri, bitmap)
            }
        }

        return uri.toString()
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

    private fun createBitmapFileAndGetPath(context: Context?, uri: Uri, bitmap: Bitmap?): String {
        val directoryPath: String = context?.externalCacheDir?.path + "/covers"
        val directory = File(directoryPath)
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, "${uri.lastPathSegment}.png")
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