package com.pghaz.revery.settings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R
import com.pghaz.revery.adapter.alarm.DefaultMediaViewHolder
import com.pghaz.revery.alarm.AlarmHandler
import com.pghaz.revery.alarm.CreateEditAlarmFragment
import com.pghaz.revery.animation.AnimatorUtils
import com.pghaz.revery.battery.PowerSettingsActivity
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.model.app.alarm.AlarmMetadata
import com.pghaz.revery.model.app.alarm.MediaType
import com.pghaz.revery.permission.PermissionDialogFactory
import com.pghaz.revery.permission.PermissionManager
import com.pghaz.revery.permission.ReveryPermission
import com.pghaz.revery.ringtone.AudioPickerHelper
import com.pghaz.revery.spotify.BaseSpotifyActivity
import com.pghaz.revery.spotify.BaseSpotifyBottomSheetDialogFragment
import com.pghaz.revery.util.Arguments
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import kotlinx.android.synthetic.main.floating_action_buttons_music_menu.*
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseSpotifyBottomSheetDialogFragment() {

    private lateinit var openMenuMusicAnimation: AnimatorSet
    private lateinit var closeMenuMusicAnimation: AnimatorSet

    override fun getLayoutResId(): Int {
        return R.layout.fragment_settings
    }

    override fun onSpotifyAuthorizedAndAvailable() {
        if (activity is BaseSpotifyActivity) {
            updateSpotifyViews(
                (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.getCurrentUser(),
                true
            )
        }
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        // Alarm preview
        testAlarmButton.setOnClickListener {
            val defaultUri = SettingsHandler.getDefaultAudioUri(testAlarmButton.context)
            val audioMetadata: AudioPickerHelper.AudioMetadata =
                AudioPickerHelper.getAudioMetadata(testAlarmButton.context, defaultUri)

            val metadata = AlarmMetadata().apply {
                this.uri = defaultUri.toString()
                this.href = null
                this.type = MediaType.DEFAULT
                this.name = audioMetadata.name
                this.description = audioMetadata.description
                this.imageUrl = audioMetadata.imageUrl
            }

            AlarmHandler.fireAlarmNow(
                testAlarmButton.context,
                delayInSeconds = 1,
                metadata,
                SettingsHandler.getFadeInDuration(testAlarmButton.context)
            )
        }

        // Slide to turn off
        context?.let {
            val slideToTurnOffEnabled = SettingsHandler.getSlideToTurnOff(it)
            slideToTurnOffSwitch.isChecked = slideToTurnOffEnabled
        }
        slideToTurnOffSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsHandler.setSlideToTurnOff(buttonView.context, isChecked)
        }

        // Snooze
        context?.let {
            var snoozeDurationPosition = SettingsHandler.getSnoozeDurationPosition(it)

            if (snoozeDurationPosition >= SnoozeDuration.values().size) {
                SettingsHandler.setSnoozeDuration(it, SettingsHandler.DEFAULT_SNOOZE_DURATION)
                snoozeDurationPosition = SettingsHandler.getSnoozeDurationPosition(it)
            }

            snoozeDurationSpinner.setSelection(snoozeDurationPosition)
        }

        snoozeDurationSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                if (context == null) {
                    return
                }

                when (position) {
                    SnoozeDuration.FIVE_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.FIVE_MINUTES)
                    }
                    SnoozeDuration.TEN_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.TEN_MINUTES)
                    }
                    SnoozeDuration.FIFTEEN_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.FIFTEEN_MINUTES)
                    }
                    SnoozeDuration.TWENTY_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.TWENTY_MINUTES)
                    }
                    SnoozeDuration.THIRTY_MINUTES.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.THIRTY_MINUTES)
                    }
                    SnoozeDuration.ONE_HOUR.ordinal -> {
                        SettingsHandler.setSnoozeDuration(context!!, SnoozeDuration.ONE_HOUR)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        // Can change snooze duration
        context?.let {
            val canChangeSnoozeDuration = SettingsHandler.getCanChangeSnoozeDuration(it)
            canChangeSnoozeDurationSwitch.isChecked = canChangeSnoozeDuration
        }
        canChangeSnoozeDurationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsHandler.setCanChangeSnoozeDuration(buttonView.context, isChecked)
        }

        // Double tap to snooze
        context?.let {
            val doubleTapSnoozeEnabled = SettingsHandler.isDoubleTapSnoozeEnabled(it)
            doubleTapSnoozeSwitch.isChecked = doubleTapSnoozeEnabled
        }
        doubleTapSnoozeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsHandler.setDoubleTapSnooze(buttonView.context, isChecked)
        }

        // Fade In
        context?.let {
            var fadeInDurationPosition = SettingsHandler.getFadeInDurationPosition(it)

            if (fadeInDurationPosition >= FadeInDuration.values().size) {
                SettingsHandler.setFadeInDuration(it, SettingsHandler.DEFAULT_FADE_IN_DURATION)
                fadeInDurationPosition = SettingsHandler.getFadeInDurationPosition(it)
            }

            fadeInDurationSpinner.setSelection(fadeInDurationPosition)
        }

        fadeInDurationSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                if (context == null) {
                    return
                }

                when (position) {
                    FadeInDuration.TEN_SECONDS.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.TEN_SECONDS)
                    }
                    FadeInDuration.TWENTY_SECONDS.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.TWENTY_SECONDS)
                    }
                    FadeInDuration.THIRTY_SECONDS.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.THIRTY_SECONDS)
                    }
                    FadeInDuration.ONE_MINUTE.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.ONE_MINUTE)
                    }
                    FadeInDuration.TWO_MINUTES.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.TWO_MINUTES)
                    }
                    FadeInDuration.FIVE_MINUTES.ordinal -> {
                        SettingsHandler.setFadeInDuration(context!!, FadeInDuration.FIVE_MINUTES)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        // Volume
        context?.let {
            val shouldUseDeviceVolume = SettingsHandler.getShouldUseDeviceVolume(it)
            shouldUseDeviceVolumeSwitch.isChecked = shouldUseDeviceVolume
            volumeAlarmSlider.isEnabled = !shouldUseDeviceVolume
        }

        shouldUseDeviceVolumeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SettingsHandler.setShouldUseDeviceVolume(buttonView.context, isChecked)

            volumeAlarmSlider.isEnabled = !isChecked
        }

        // Volume Alarm
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        volumeAlarmSlider.valueFrom = 0f
        volumeAlarmSlider.valueTo = maxVolume.toFloat()
        volumeAlarmSlider.value =
            SettingsHandler.getAlarmVolume(volumeAlarmSlider.context, maxVolume).toFloat()
        volumeAlarmSlider.addOnChangeListener { slider, value, _ ->
            SettingsHandler.setAlarmVolume(slider.context, value.toInt())
        }

        // Battery Optimization
        batteryOptimizationButton.setOnClickListener {
            context?.let {
                val intent = Intent(it, PowerSettingsActivity::class.java)
                startActivity(intent)
            }
        }

        // About
        aboutButton.setOnClickListener {
            showAboutDialog()
        }

        // Default Audio
        chooseRingtoneButton.setOnClickListener {
            if (chooseRingtoneButton.isExpanded) {
                closeMusicMenu()
            } else {
                openMusicMenu()
            }
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

        initDefaultAudioViews()

        if (activity is BaseSpotifyActivity) {
            updateSpotifyViews(
                (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.getCurrentUser(),
                false
            )
        }
    }

    private fun initDefaultAudioViews() {
        context?.let {
            val defaultUri = SettingsHandler.getDefaultAudioUri(it)

            val audioMetadata: AudioPickerHelper.AudioMetadata =
                AudioPickerHelper.getAudioMetadata(context, defaultUri)

            val metadata = AlarmMetadata().apply {
                this.uri = defaultUri.toString()
                this.href = null
                this.type = MediaType.DEFAULT
                this.name = audioMetadata.name
                this.description = audioMetadata.description
                this.imageUrl = audioMetadata.imageUrl
            }

            updateDefaultAlarmViews(metadata)
        }
    }

    private fun openMusicMenu() {
        val musicPickerAnimator = AnimatorUtils.getTranslationAnimatorSet(
            musicPickerButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            0,
            400
        )

        val ringtonePickerAnimator = AnimatorUtils.getTranslationAnimatorSet(
            ringtonePickerButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            0,
            500
        )

        openMenuMusicAnimation = AnimatorSet()
        openMenuMusicAnimation.playTogether(
            musicPickerAnimator,
            ringtonePickerAnimator
        )

        if (this::closeMenuMusicAnimation.isInitialized && closeMenuMusicAnimation.isRunning) {
            closeMenuMusicAnimation.cancel()
        }

        openMenuMusicAnimation.start()

        chooseRingtoneButton.isExpanded = true
        chooseRingtoneButton.setImageResource(R.drawable.ic_close)
    }

    private fun closeMusicMenu() {
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
            musicPickerAnimator,
            ringtonePickerAnimator
        )

        if (this::openMenuMusicAnimation.isInitialized && openMenuMusicAnimation.isRunning) {
            openMenuMusicAnimation.cancel()
        }

        closeMenuMusicAnimation.start()

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
                AudioPickerHelper.startMusicPickerForResult(
                    this,
                    CreateEditAlarmFragment.REQUEST_CODE_PICK_MUSIC
                )
            }
        }
    }

    private fun openRingtonePicker() {
        AudioPickerHelper.showRingtonePicker(
            context,
            childFragmentManager
        ) { ringtoneName, ringtoneUri ->
            handleRingtonePickerSelection(ringtoneName, ringtoneUri)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CreateEditAlarmFragment.REQUEST_CODE_PICK_MUSIC && resultCode == Activity.RESULT_OK) {
            handleMusicPickerSelection(data)
        }
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
                        AudioPickerHelper.startMusicPickerForResult(
                            this,
                            CreateEditAlarmFragment.REQUEST_CODE_PICK_MUSIC
                        )
                    }

                    PermissionManager.setStoragePermissionHasBeenAsked(it, true)
                }
            }
        }
    }

    private fun handleMusicPickerSelection(data: Intent?) {
        val ringtoneUri: Uri? = data?.data

        if (ringtoneUri != null) {
            context?.let {
                AudioPickerHelper.grantPermissionForUri(it, data)

                val audioMetadata: AudioPickerHelper.AudioMetadata =
                    AudioPickerHelper.getAudioMetadata(it, ringtoneUri)

                val metadata = AlarmMetadata().apply {
                    this.uri = ringtoneUri.toString()
                    this.href = null
                    this.type = MediaType.DEFAULT
                    this.name = audioMetadata.name
                    this.description = audioMetadata.description
                    this.imageUrl = audioMetadata.imageUrl
                }

                updateDefaultAlarmViews(metadata)

                SettingsHandler.setDefaultAudioUri(it, ringtoneUri)
            }
        }
    }

    private fun handleRingtonePickerSelection(ringtoneName: String, ringtoneUri: Uri?) {
        if (ringtoneUri != null) {
            context?.let {
                val metadata = AlarmMetadata().apply {
                    this.uri = ringtoneUri.toString()
                    this.href = null
                    this.type = MediaType.DEFAULT
                    this.name = ringtoneName
                    this.description = null
                    this.imageUrl = ringtoneUri.toString()
                }

                updateDefaultAlarmViews(metadata)

                SettingsHandler.setDefaultAudioUri(it, ringtoneUri)
            }
        }
    }

    private fun updateDefaultAlarmViews(metadata: AlarmMetadata) {
        ringtoneInfoContainer.removeAllViews()

        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_view_alarm_media_square, ringtoneInfoContainer, false)
        val holder = DefaultMediaViewHolder(view)

        holder.bind(metadata)

        val params = view.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.ALIGN_PARENT_START)
        ringtoneInfoContainer.addView(view, params)
    }

    @SuppressLint("InflateParams")
    private fun showAboutDialog() {
        context?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_settings_about, null)
            val versionTextView = view.findViewById<TextView>(R.id.versionTextView)
            versionTextView.text = String.format(
                "%s %s (%d)",
                it.getString(R.string.about_version),
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )

            val dialog = AlertDialog.Builder(it).apply {
                setCancelable(true)
                setView(view)
            }.create()
            dialog.show()

            val aboutOKButton = view.findViewById<AppCompatButton>(R.id.aboutOKButton)
            aboutOKButton.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    private fun updateSpotifyViews(spotifyUser: UserPrivate?, animate: Boolean) {
        if (spotifyUser?.id == null) {
            if (animate) {
                loginSpotifyButton.apply {
                    alpha = 0f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setListener(null)
                }

                loggedContainer.apply {
                    alpha = 1f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                loggedContainer.visibility = View.INVISIBLE
                            }
                        })
                }
            } else {
                loginSpotifyButton.apply {
                    alpha = 1f
                    visibility = View.VISIBLE
                }

                loggedContainer.apply {
                    alpha = 1f
                    visibility = View.GONE
                }
            }
        } else {
            if (animate) {
                loginSpotifyButton.apply {
                    alpha = 1f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                loginSpotifyButton.visibility = View.INVISIBLE
                            }
                        })
                }

                loggedContainer.apply {
                    alpha = 0f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setListener(null)
                }
            } else {
                loginSpotifyButton.apply {
                    alpha = 1f
                    visibility = View.GONE
                }

                loggedContainer.apply {
                    alpha = 1f
                    visibility = View.VISIBLE
                }
            }

            pseudoTextView.text = spotifyUser.display_name

            val imageUrl = if (spotifyUser.images != null && spotifyUser.images.size > 0) {
                spotifyUser.images[0].url
            } else {
                null
            }

            profileImageView.clipToOutline = true
            ImageLoader.get()
                .placeholder(R.drawable.placeholder_circle)
                .load(imageUrl)
                .into(profileImageView)
        }

        loginSpotifyButton.setOnClickListener {
            if (activity is BaseSpotifyActivity) {
                (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.authorize(
                    activity as BaseSpotifyActivity,
                    BaseSpotifyActivity.REQUEST_CODE_SPOTIFY_LOGIN
                )
            }
        }

        logoutSpotifyButton.setOnClickListener {
            if (activity is BaseSpotifyActivity) {
                (activity as BaseSpotifyActivity).spotifyAuthClient.logOut()
                updateSpotifyViews(
                    (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.getCurrentUser(),
                    true
                )
            }
        }
    }

    companion object {
        const val TAG = "SettingsFragment"

        fun newInstance(title: String?): SettingsFragment {
            val fragment = SettingsFragment()

            val args = Bundle()
            args.putInt(Arguments.ARGS_DIALOG_CLOSE_ICON, R.drawable.ic_validate)
            args.putString(Arguments.ARGS_DIALOG_TITLE, title)

            fragment.arguments = args

            return fragment
        }
    }
}