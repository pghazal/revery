package com.pghaz.revery

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.widget.RelativeLayout
import androidx.annotation.CallSuper
import com.pghaz.revery.adapter.alarm.DefaultMediaViewHolder
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyAlbumViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyArtistViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyPlaylistViewHolder
import com.pghaz.revery.adapter.spotify.SpotifyTrackViewHolder
import com.pghaz.revery.animation.AnimatorUtils
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.MediaType
import com.pghaz.revery.model.app.spotify.AlbumWrapper
import com.pghaz.revery.model.app.spotify.ArtistWrapper
import com.pghaz.revery.model.app.spotify.PlaylistWrapper
import com.pghaz.revery.model.app.spotify.TrackWrapper
import com.pghaz.revery.permission.PermissionDialogFactory
import com.pghaz.revery.permission.PermissionManager
import com.pghaz.revery.permission.ReveryPermission
import com.pghaz.revery.ringtone.AudioPickerHelper
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.spotify.SpotifyActivity
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.ViewUtils
import kotlinx.android.synthetic.main.floating_action_buttons_music_menu.*
import kotlinx.android.synthetic.main.fragment_alarm_create_edit.*

abstract class BaseCreateEditFragment : BaseBottomSheetDialogFragment() {

    private lateinit var chooseRingtoneButtonAnimatorSet: AnimatorSet
    private lateinit var openMenuMusicAnimation: AnimatorSet
    private lateinit var closeMenuMusicAnimation: AnimatorSet

    protected fun updateMetadataViews(metadata: MediaMetadata) {
        if (metadata.type != MediaType.DEFAULT) {
            moreOptionsButton.visibility = View.VISIBLE
        } else {
            moreOptionsButton.visibility = View.GONE
        }

        ringtoneInfoContainer.removeAllViews()
        ringtoneInfoContainer.visibility = View.VISIBLE

        val view: View
        val holder: BaseViewHolder

        when (metadata.type) {
            MediaType.DEFAULT -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_view_alarm_media_square, ringtoneInfoContainer, false)
                holder = DefaultMediaViewHolder(view)
            }

            MediaType.SPOTIFY_TRACK -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_view_alarm_media_square, ringtoneInfoContainer, false)
                holder = SpotifyTrackViewHolder(view)
            }

            MediaType.SPOTIFY_ALBUM -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_view_alarm_media_square, ringtoneInfoContainer, false)
                holder = SpotifyAlbumViewHolder(view)
            }

            MediaType.SPOTIFY_ARTIST -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_view_alarm_media_round, ringtoneInfoContainer, false)
                holder = SpotifyArtistViewHolder(view)
            }

            MediaType.SPOTIFY_PLAYLIST -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.item_view_alarm_media_square, ringtoneInfoContainer, false)
                holder = SpotifyPlaylistViewHolder(view)
            }
        }

        holder.bind(metadata)

        val params = view.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.ALIGN_PARENT_START)
        ringtoneInfoContainer.addView(view, params)

        ImageLoader.get()
            .load(metadata.imageUrl)
            .blur()
            .roundCorners(16, 0)
            .ratioAndWidth(1f, ViewUtils.getRealScreenWidthSize(backgroundImageView.context), true)
            .into(backgroundImageView)
    }

    @CallSuper
    override fun configureViews(savedInstanceState: Bundle?) {
        chooseRingtoneButton.setOnClickListener {
            if (chooseRingtoneButton.isExpanded) {
                closeMusicMenu()
            } else {
                openMusicMenu()
            }
        }

        floatingMenuTouchInterceptor.setOnTouchListener(OnTouchListener { view, _ ->
            if (chooseRingtoneButton.isExpanded) {
                closeMusicMenu()
            }
            return@OnTouchListener view.performClick()
        })

        spotifyButton.setOnClickListener {
            if (chooseRingtoneButton.isExpanded) {
                closeMusicMenu()
            }
            openSpotifyActivity()
        }

        musicPickerButton.setOnClickListener {
            if (chooseRingtoneButton.isExpanded) {
                closeMusicMenu()
            }
            openMusicPicker()
        }

        ringtonePickerButton.setOnClickListener {
            if (chooseRingtoneButton.isExpanded) {
                closeMusicMenu()
            }
            openRingtonePicker()
        }

        defaultRingtoneButton.setOnClickListener {
            if (chooseRingtoneButton.isExpanded) {
                closeMusicMenu()
            }
            val metadata = getDefaultRingtoneMetadata()
            handleDefaultRingtoneClicked(metadata)
        }

        startChooseRingtoneButtonAnimation()
    }

    private fun startChooseRingtoneButtonAnimation() {
        val scaleXPlayButtonAnimator = ObjectAnimator.ofFloat(
            chooseRingtoneButton,
            View.SCALE_X, 1f, 0.8f
        )
        scaleXPlayButtonAnimator.repeatCount = ObjectAnimator.INFINITE
        scaleXPlayButtonAnimator.repeatMode = ObjectAnimator.REVERSE
        scaleXPlayButtonAnimator.duration = 1000L

        val scaleYPlayButtonAnimator = ObjectAnimator.ofFloat(
            chooseRingtoneButton,
            View.SCALE_Y, 1f, 0.8f
        )
        scaleYPlayButtonAnimator.repeatCount = ObjectAnimator.INFINITE
        scaleYPlayButtonAnimator.repeatMode = ObjectAnimator.REVERSE
        scaleYPlayButtonAnimator.duration = 1000L

        chooseRingtoneButtonAnimatorSet = AnimatorSet()
        chooseRingtoneButtonAnimatorSet.play(scaleXPlayButtonAnimator)
            .with(scaleYPlayButtonAnimator)
        chooseRingtoneButtonAnimatorSet.start()
    }

    private fun getDefaultRingtoneMetadata(): MediaMetadata {
        return MediaMetadata().apply {
            context?.let { nonNullContext ->
                val uri = SettingsHandler.getDefaultAudioUri(nonNullContext)

                val audioMetadata: AudioPickerHelper.AudioMetadata =
                    AudioPickerHelper.getAudioMetadata(nonNullContext, uri)

                this.type = MediaType.DEFAULT
                this.uri = uri.toString()
                this.name = audioMetadata.name
                this.description = audioMetadata.description
                this.imageUrl = audioMetadata.imageUrl
            }
        }
    }

    private fun openSpotifyActivity() {
        val intent = Intent(context, SpotifyActivity::class.java)
        startActivityForResult(intent, SpotifyActivity.REQUEST_CODE_SPOTIFY_SEARCH)
    }

    private fun openMusicMenu() {
        val defaultRingtoneAnimator = AnimatorUtils.getTranslationAnimatorSet(
            defaultRingtoneButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            0,
            400
        )

        val spotifyAnimator = AnimatorUtils.getTranslationAnimatorSet(
            spotifyButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            0,
            500
        )

        val musicPickerAnimator = AnimatorUtils.getTranslationAnimatorSet(
            musicPickerButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            0,
            600
        )

        val ringtonePickerAnimator = AnimatorUtils.getTranslationAnimatorSet(
            ringtonePickerButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            0,
            700
        )

        openMenuMusicAnimation = AnimatorSet()
        openMenuMusicAnimation.playTogether(
            defaultRingtoneAnimator,
            spotifyAnimator,
            musicPickerAnimator,
            ringtonePickerAnimator
        )

        if (this::closeMenuMusicAnimation.isInitialized && closeMenuMusicAnimation.isRunning) {
            closeMenuMusicAnimation.cancel()
        }

        AnimatorUtils.fadeIn(floatingMenuTouchInterceptor, 300, 0)
        openMenuMusicAnimation.start()
        chooseRingtoneButtonAnimatorSet.pause()

        chooseRingtoneButton.isExpanded = true
        chooseRingtoneButton.setImageResource(R.drawable.ic_close)
    }

    private fun closeMusicMenu() {
        val defaultRingtoneAnimator = AnimatorUtils.getTranslationAnimatorSet(
            defaultRingtoneButton,
            false,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_TOP_TO_BOTTOM,
            true,
            0,
            400
        )

        val spotifyAnimator = AnimatorUtils.getTranslationAnimatorSet(
            spotifyButton,
            false,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_TOP_TO_BOTTOM,
            true,
            0,
            400
        )

        val musicPickerAnimator = AnimatorUtils.getTranslationAnimatorSet(
            musicPickerButton,
            false,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_TOP_TO_BOTTOM,
            true,
            0,
            400
        )

        val ringtonePickerAnimator = AnimatorUtils.getTranslationAnimatorSet(
            ringtonePickerButton,
            false,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_TOP_TO_BOTTOM,
            true,
            0,
            400
        )

        closeMenuMusicAnimation = AnimatorSet()
        closeMenuMusicAnimation.playTogether(
            defaultRingtoneAnimator,
            spotifyAnimator,
            musicPickerAnimator,
            ringtonePickerAnimator
        )

        if (this::openMenuMusicAnimation.isInitialized && openMenuMusicAnimation.isRunning) {
            openMenuMusicAnimation.cancel()
        }

        AnimatorUtils.fadeOut(floatingMenuTouchInterceptor, 300, 0)
        closeMenuMusicAnimation.start()
        chooseRingtoneButtonAnimatorSet.resume()

        chooseRingtoneButton.isExpanded = false
        chooseRingtoneButton.setImageResource(R.drawable.ic_music_note)
    }

    private fun openMusicPicker() {
        activity?.let {
            val permission = ReveryPermission.WRITE_EXTERNAL_STORAGE

            if (PermissionManager.isBlocked(it, permission) &&
                !PermissionManager.hasPermissionBeenGranted(it, permission)
            ) {
                PermissionDialogFactory.showPermissionDialog(it)
            } else if (!PermissionManager.hasPermissionBeenGranted(it, permission)) {
                PermissionManager.askForPermission(this, permission)
            } else {
                AudioPickerHelper.startMusicPickerForResult(this, REQUEST_CODE_PICK_MUSIC)
            }
        }
    }

    private fun openRingtonePicker() {
        AudioPickerHelper.showRingtonePicker(
            context,
            childFragmentManager
        ) { ringtoneName, ringtoneUri ->
            handleRingtoneSelection(ringtoneName, ringtoneUri)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SpotifyActivity.REQUEST_CODE_SPOTIFY_SEARCH && resultCode == Activity.RESULT_OK) {
            handleSpotifySelectionResponse(data)
        } else if (requestCode == REQUEST_CODE_PICK_MUSIC && resultCode == Activity.RESULT_OK) {
            handleMusicSelection(data)
        }
    }

    abstract fun handleDefaultRingtoneClicked(metadata: MediaMetadata)
    abstract fun handleMusicSelection(metadata: MediaMetadata)
    abstract fun handleRingtoneSelection(metadata: MediaMetadata)
    abstract fun handleSpotifySelection(metadata: MediaMetadata)

    private fun handleSpotifySelectionResponse(data: Intent?) {
        val selectedSpotifyItem =
            data?.getParcelableExtra(Arguments.ARGS_SPOTIFY_ITEM_SELECTED) as BaseModel?

        if (selectedSpotifyItem != null) {
            val metadata = spotifyItemToMetadata(selectedSpotifyItem)
            handleSpotifySelection(metadata)
        }
    }

    private fun spotifyItemToMetadata(item: BaseModel): MediaMetadata {
        return when (item) {
            is TrackWrapper -> {
                item.toAlarmMetadata()
            }
            is AlbumWrapper -> {
                item.toMediaMetadata()
            }
            is ArtistWrapper -> {
                item.toMediaMetadata()
            }
            is PlaylistWrapper -> {
                item.toAlarmMetadata()
            }
            else -> {
                MediaMetadata()
            }
        }
    }

    private fun handleMusicSelection(data: Intent?) {
        val ringtoneUri: Uri? = data?.data

        if (ringtoneUri != null) {
            AudioPickerHelper.grantPermissionForUri(context, data)

            val audioMetadata: AudioPickerHelper.AudioMetadata =
                AudioPickerHelper.getAudioMetadata(context, ringtoneUri)

            val metadata = MediaMetadata().apply {
                uri = ringtoneUri.toString()
                href = null
                type = MediaType.DEFAULT
                name = audioMetadata.name
                description = audioMetadata.description
                imageUrl = audioMetadata.imageUrl
            }

            handleMusicSelection(metadata)
        }
    }

    private fun handleRingtoneSelection(ringtoneName: String, ringtoneUri: Uri?) {
        val metadata = MediaMetadata().apply {
            this.uri = ringtoneUri.toString()
            this.href = null
            this.type = MediaType.DEFAULT
            this.name = ringtoneName
            this.description = null
            this.imageUrl = ringtoneUri.toString()
        }

        handleRingtoneSelection(metadata)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val storagePermission = ReveryPermission.WRITE_EXTERNAL_STORAGE

        when (requestCode) {
            storagePermission.requestCode -> {
                context?.let {
                    if (PermissionManager.hasPermissionBeenGranted(it, storagePermission)) {
                        AudioPickerHelper.startMusicPickerForResult(this, REQUEST_CODE_PICK_MUSIC)
                    }

                    PermissionManager.setStoragePermissionHasBeenAsked(it, true)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PICK_MUSIC = 21
    }
}