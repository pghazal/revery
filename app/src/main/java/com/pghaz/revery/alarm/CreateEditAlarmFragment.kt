package com.pghaz.revery.alarm

import android.animation.AnimatorSet
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.BaseCreateEditFragment
import com.pghaz.revery.R
import com.pghaz.revery.animation.AnimatorUtils
import com.pghaz.revery.model.app.Alarm
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.MediaType
import com.pghaz.revery.ringtone.AudioPickerHelper
import com.pghaz.revery.settings.SettingsFragment
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.DateTimeUtils
import com.pghaz.revery.viewmodel.alarm.CreateEditAlarmViewModel
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.floating_action_buttons_music_menu_alarms.*
import kotlinx.android.synthetic.main.fragment_alarm_create_edit.*
import java.util.*

class CreateEditAlarmFragment : BaseCreateEditFragment() {

    private lateinit var openMenuMusicAnimation: AnimatorSet
    private lateinit var closeMenuMusicAnimation: AnimatorSet

    private lateinit var createEditAlarmViewModel: CreateEditAlarmViewModel
    private var alarm: Alarm? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_alarm_create_edit
    }

    override fun parseArguments(arguments: Bundle?) {
        super.parseArguments(arguments)
        arguments?.let {
            alarm = it.getParcelable(Arguments.ARGS_ALARM)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Arguments.ARGS_ALARM, alarm)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createEditAlarmViewModel = ViewModelProvider(this).get(CreateEditAlarmViewModel::class.java)
        createEditAlarmViewModel.timeChangedAlarmLiveData.observe(this, { updatedAlarm ->
            val timeRemainingInfo = DateTimeUtils.getTimeRemaining(updatedAlarm)
            timeRemainingTextView.text =
                DateTimeUtils.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)
        })

        createEditAlarmViewModel.metadataLiveData.observe(this, { metadata ->
            alarm?.metadata = metadata
            updateMetadataViews(metadata)
        })
    }

    private fun initTimePicker(is24HourFormat: Boolean) {
        context?.let {
            val face = ResourcesCompat.getFont(it, R.font.montserrat_regular)
            hourNumberPicker.setSelectedTypeface(face)
            minuteNumberPicker.setSelectedTypeface(face)
            hourNumberPicker.typeface = face
            minuteNumberPicker.typeface = face
            hourNumberPicker.formatter = NumberPicker.getTwoDigitFormatter()
            minuteNumberPicker.formatter = NumberPicker.getTwoDigitFormatter()

            if (is24HourFormat) {
                amPmContainer.visibility = View.GONE
                hourNumberPicker.maxValue = it.resources.getInteger(R.integer.format_24_hour_max)
                hourNumberPicker.minValue = it.resources.getInteger(R.integer.format_24_hour_min)
            } else {
                amPmContainer.visibility = View.VISIBLE
                hourNumberPicker.maxValue = it.resources.getInteger(R.integer.format_12_hour_max)
                hourNumberPicker.minValue = it.resources.getInteger(R.integer.format_12_hour_min)
            }
        }
    }

    private fun configureTimePickerNow(is24HourFormat: Boolean) {
        val calendar = Calendar.getInstance()
        minuteNumberPicker.value = DateTimeUtils.getCurrentMinute(calendar)

        if (is24HourFormat) {
            hourNumberPicker.value = DateTimeUtils.getCurrentHourOfDay(calendar)
        } else {
            val amPM = calendar.get(Calendar.AM_PM)
            if (amPM == Calendar.AM) {
                amRadioButton.isChecked = true
                pmRadioButton.isChecked = false
            } else {
                amRadioButton.isChecked = false
                pmRadioButton.isChecked = true
            }
            hourNumberPicker.value = DateTimeUtils.getCurrentHour(calendar)
        }
    }

    private fun configureTimePickerFromAlarm(alarm: Alarm, is24HourFormat: Boolean) {
        val alarmHour = alarm.hour

        hourNumberPicker.value = if (is24HourFormat) {
            alarmHour
        } else {
            DateTimeUtils.get12HourFormatFrom24HourFormat(alarmHour)
        }
        minuteNumberPicker.value = alarm.minute

        amRadioButton.isChecked = DateTimeUtils.isAM(alarmHour)
        pmRadioButton.isChecked = !DateTimeUtils.isAM(alarmHour)
    }

    private fun configureViewsFromAlarm(alarm: Alarm) {
        labelEditText.setText(alarm.label)

        mondayToggle.isChecked = alarm.monday
        tuesdayToggle.isChecked = alarm.tuesday
        wednesdayToggle.isChecked = alarm.wednesday
        thursdayToggle.isChecked = alarm.thursday
        fridayToggle.isChecked = alarm.friday
        saturdayToggle.isChecked = alarm.saturday
        sundayToggle.isChecked = alarm.sunday

        vibrateToggle.isChecked = alarm.vibrate
        fadeInToggle.isChecked = alarm.fadeIn

        createEditAlarmViewModel.metadataLiveData.value = alarm.metadata
    }

    private fun setAlarmHour(is24HourFormat: Boolean, hour: Int) {
        alarm?.hour = if (is24HourFormat) {
            hour
        } else {
            DateTimeUtils.get24HourFormatFrom12HourFormat(
                hour,
                amRadioButton.isChecked
            )
        }
    }

    private fun setAlarmMinute(minute: Int) {
        alarm?.minute = minute
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)

        val is24HourFormat = DateFormat.is24HourFormat(context)

        initTimePicker(is24HourFormat)

        // If we're editing an alarm, we set time/minute on the time picker
        if (alarm?.id != BaseModel.NO_ID) {
            configureTimePickerFromAlarm(alarm!!, is24HourFormat)
            // Set views data before any listeners are set
            configureViewsFromAlarm(alarm!!)
            // Also show negative button (delete button)
            negativeButton.visibility = View.VISIBLE
        } else {
            configureTimePickerNow(is24HourFormat)
            setAlarmHour(is24HourFormat, hourNumberPicker.value)
            setAlarmMinute(minuteNumberPicker.value)
            negativeButton.visibility = View.GONE
        }

        // Notify the LiveData so that it updates the time remaining
        createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm

        positiveButton.setOnClickListener {
            // This is a creation
            if (BaseModel.NO_ID == alarm?.id) {
                createAndScheduleAlarm()
            } else { // This is an update
                editAndScheduleAlarm()
            }

            dismiss()
        }

        negativeButton.setOnClickListener {
            // If we were creating an alarm and click the negative button, just dismiss the dialog
            if (BaseModel.NO_ID == alarm?.id) {
                // do nothing
            } else { // otherwise delete the existing alarm
                createEditAlarmViewModel.delete(context, alarm!!)
            }

            dismiss()
        }

        hourNumberPicker.setOnValueChangedListener { _, _, hour ->
            setAlarmHour(is24HourFormat, hour)
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        minuteNumberPicker.setOnValueChangedListener { _, _, minute ->
            setAlarmMinute(minute)
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        amRadioButton.setOnCheckedChangeListener { _, _ ->
            setAlarmHour(is24HourFormat, hourNumberPicker.value)
            setAlarmMinute(minuteNumberPicker.value)
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        pmRadioButton.setOnCheckedChangeListener { _, _ ->
            setAlarmHour(is24HourFormat, hourNumberPicker.value)
            setAlarmMinute(minuteNumberPicker.value)
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        mondayToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.monday = mondayToggle.isChecked
            alarm?.recurring = isAlarmRecurring()
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        tuesdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.tuesday = tuesdayToggle.isChecked
            alarm?.recurring = isAlarmRecurring()
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        wednesdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.wednesday = wednesdayToggle.isChecked
            alarm?.recurring = isAlarmRecurring()
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        thursdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.thursday = thursdayToggle.isChecked
            alarm?.recurring = isAlarmRecurring()
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        fridayToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.friday = fridayToggle.isChecked
            alarm?.recurring = isAlarmRecurring()
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        saturdayToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.saturday = saturdayToggle.isChecked
            alarm?.recurring = isAlarmRecurring()
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        sundayToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.sunday = sundayToggle.isChecked
            alarm?.recurring = isAlarmRecurring()
            createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm
        }

        vibrateToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.vibrate = vibrateToggle.isChecked
        }

        fadeInToggle.setOnCheckedChangeListener { _, _ ->
            alarm?.fadeIn = fadeInToggle.isChecked
            // This is not really useful for now because all alarms have same fade in duration
            //alarm.fadeInDuration = SettingsHandler.getFadeInDuration(buttonView.context)
        }

        moreOptionsButton.setOnClickListener {
            showMoreOptionsFragment()
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
            val metadata = getAlarmDefaultRingtoneMetadata()
            handleDefaultRingtoneClicked(metadata)
        }
    }

    override fun openMusicMenu() {
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

    override fun closeMusicMenu() {
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

    private fun showMoreOptionsFragment() {
        var fragment =
            childFragmentManager.findFragmentByTag(SettingsFragment.TAG) as MoreOptionsAlarmFragment?
        if (fragment == null) {
            fragment = MoreOptionsAlarmFragment.newInstance(
                getString(R.string.more_options),
                alarm
            )
            fragment.show(childFragmentManager, MoreOptionsAlarmFragment.TAG)
        }
    }

    private fun isAlarmRecurring(): Boolean {
        return mondayToggle.isChecked ||
                tuesdayToggle.isChecked ||
                wednesdayToggle.isChecked ||
                thursdayToggle.isChecked ||
                fridayToggle.isChecked ||
                saturdayToggle.isChecked ||
                sundayToggle.isChecked
    }

    private fun createAndScheduleAlarm() {
        val label: String =
            if (TextUtils.isEmpty(labelEditText.text?.trim())) "" else labelEditText.text?.trim()
                .toString()

        alarm?.let {
            it.id = System.currentTimeMillis()
            it.label = label
            it.enabled = true

            // Safe init of uri
            if (it.metadata.uri.isNullOrEmpty()) {
                it.metadata = MediaMetadata().apply {
                    context?.let { nonNullContext ->
                        val uri = SettingsHandler.getAlarmDefaultAudioUri(nonNullContext)

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

            createEditAlarmViewModel.createAlarm(context, it)
        }
    }

    private fun editAndScheduleAlarm() {
        val label: String =
            if (TextUtils.isEmpty(labelEditText.text?.trim())) "" else labelEditText.text?.trim()
                .toString()

        alarm?.let {
            it.label = label
            it.enabled = true

            createEditAlarmViewModel.editAlarm(context, it)
        }
    }

    override fun handleDefaultRingtoneClicked(metadata: MediaMetadata) {
        createEditAlarmViewModel.metadataLiveData.value = metadata
    }

    override fun handleMusicSelection(metadata: MediaMetadata) {
        createEditAlarmViewModel.metadataLiveData.value = metadata
    }

    override fun handleRingtoneSelection(metadata: MediaMetadata) {
        createEditAlarmViewModel.metadataLiveData.value = metadata
    }

    override fun handleSpotifySelection(metadata: MediaMetadata) {
        alarm?.let {
            metadata.apply {
                this.shuffle = it.metadata.shuffle
                this.shouldKeepPlaying = it.metadata.shouldKeepPlaying
                this.repeat = it.metadata.repeat
            }

            createEditAlarmViewModel.metadataLiveData.value = metadata
        }
    }

    companion object {
        const val TAG = "CreateEditAlarmFragment"

        fun newInstance(dialogTitle: String): CreateEditAlarmFragment {
            val newAlarm = Alarm(
                id = BaseModel.NO_ID, // Let this ´NO_ID´ value. It defines if it's creation or edition of alarm
                hour = 0,
                minute = 0,
                label = "",
                enabled = true,
                recurring = false,
                monday = false,
                tuesday = false,
                wednesday = false,
                thursday = false,
                friday = false,
                saturday = false,
                sunday = false,
                vibrate = false,
                fadeIn = false,
                fadeInDuration = 0,
                metadata = MediaMetadata()
            )

            return newInstance(dialogTitle, newAlarm)
        }

        fun newInstance(
            dialogTitle: String,
            alarm: Alarm
        ): CreateEditAlarmFragment {
            val args = Bundle()

            args.putString(Arguments.ARGS_DIALOG_TITLE, dialogTitle)
            args.putParcelable(Arguments.ARGS_ALARM, alarm)

            val fragment = CreateEditAlarmFragment()
            fragment.arguments = args

            return fragment
        }
    }
}