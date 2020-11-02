package com.pghaz.revery.ringtone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.pghaz.revery.R
import com.pghaz.revery.image.ImageUtils

object AudioPickerHelper {

    class AudioMetadata {
        var name: String? = null
        var description: String? = null
        var imageUrl: String? = null
    }

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

    fun getAudioMetadata(context: Context?, uri: Uri): AudioMetadata {
        val contentResolver = context?.contentResolver

        val cursor: Cursor? = contentResolver?.query(
            uri, null, null, null, null, null
        )

        val mediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaMetadataRetriever.setDataSource(context, uri)
        } catch (ex: IllegalArgumentException) {
            // do nothing: we're probably reading a default ringtone Uri
        }

        val audioMetadata = AudioMetadata()

        cursor?.use {
            if (it.moveToFirst()) {
                audioMetadata.name =
                    when {
                        it.columnNames.contains(MediaStore.Audio.Media.TITLE) &&
                                it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE)) != null -> {
                            it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE))
                        }
                        it.columnNames.contains(MediaStore.Audio.Media.DISPLAY_NAME) &&
                                it.getString(it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)) != null -> {
                            it.getString(it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                        }
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null -> {
                            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        }
                        else -> {
                            RingtoneManager.getRingtone(context, uri)?.getTitle(context)
                                ?.substringBeforeLast(".")
                        }
                    }

                audioMetadata.description =
                    when {
                        (it.columnNames.contains(MediaStore.Audio.Media.IS_MUSIC) &&
                                it.getString(it.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) == "1" &&
                                it.columnNames.contains(MediaStore.Audio.Media.ARTIST) &&
                                it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST)) != null) -> {
                            it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                        }
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null -> {
                            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        }
                        else -> null
                    }
            }

            it.close()
        }

        audioMetadata.imageUrl = ImageUtils.getCoverArtFilePath(context, uri)

        return audioMetadata
    }
}