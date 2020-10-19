package com.pghaz.revery.alarm

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.alarm.model.app.AbstractAlarm
import com.pghaz.revery.alarm.model.app.Alarm
import com.pghaz.revery.alarm.model.app.SpotifyAlarm
import com.pghaz.revery.alarm.viewmodel.CreateEditAlarmViewModel
import com.pghaz.revery.animation.AnimatorUtils
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.settings.SettingsFragment
import com.pghaz.revery.spotify.SpotifyActivity
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.util.DateTimeUtils
import com.shawnlin.numberpicker.NumberPicker
import kaaes.spotify.webapi.android.models.PlaylistSimple
import kotlinx.android.synthetic.main.floating_action_button_menu.*
import kotlinx.android.synthetic.main.fragment_alarm_create_edit.*
import java.util.*

class CreateEditAlarmFragment : BaseBottomSheetDialogFragment() {

    private lateinit var createEditAlarmViewModel: CreateEditAlarmViewModel
    private var alarm: AbstractAlarm? = null

    private fun initAlarmFromArguments(arguments: Bundle?) {
        arguments?.let { args ->
            alarm = args.getParcelable(Arguments.ARGS_ALARM)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAlarmFromArguments(arguments)

        createEditAlarmViewModel = ViewModelProvider(this).get(CreateEditAlarmViewModel::class.java)
        createEditAlarmViewModel.timeChangedAlarmLiveData.observe(this, { updatedAlarm ->
            val timeRemainingInfo = DateTimeUtils.getTimeRemaining(updatedAlarm)
            timeRemainingTextView.text =
                DateTimeUtils.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)
        })

        createEditAlarmViewModel.alarmMetadataLiveData.observe(this, {
            when (it) {
                is Alarm -> {
                    moreOptionsButton.visibility = View.GONE
                    ringtoneInfoContainer.visibility = View.GONE
                }

                is SpotifyAlarm -> {
                    moreOptionsButton.visibility = View.VISIBLE
                    ringtoneInfoContainer.visibility = View.VISIBLE

                    if (it.name.isNullOrEmpty()) {
                        titleTextView.text = ""
                        titleTextView.visibility = View.GONE
                    } else {
                        titleTextView.text = it.name
                        titleTextView.visibility = View.VISIBLE
                    }

                    if (it.description.isNullOrEmpty()) {
                        subtitleTextView.text = ""
                        subtitleTextView.visibility = View.GONE
                    } else {
                        subtitleTextView.text = it.description
                        subtitleTextView.visibility = View.VISIBLE
                    }

                    if (it.imageUrl.isNullOrEmpty()) {
                        imageView.visibility = View.GONE
                    } else {
                        imageView.visibility = View.VISIBLE
                        ImageLoader.get().load(it.imageUrl).into(imageView)
                    }
                }
            }
        })
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_alarm_create_edit
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

    private fun configureTimePickerFromAlarm(alarm: AbstractAlarm, is24HourFormat: Boolean) {
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

    private fun configureViewsFromAlarm(alarm: AbstractAlarm) {
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

        createEditAlarmViewModel.alarmMetadataLiveData.value = alarm
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
        val is24HourFormat = DateFormat.is24HourFormat(context)

        initTimePicker(is24HourFormat)

        // If we're editing an alarm, we set time/minute on the time picker
        if (alarm?.id != BaseModel.NO_ID) {
            configureTimePickerFromAlarm(alarm!!, is24HourFormat)
            // Set views data before any listeners are set
            configureViewsFromAlarm(alarm!!)
            // Also show negative button (delete button)
            negativeAlarmButton.visibility = View.VISIBLE
        } else {
            configureTimePickerNow(is24HourFormat)
            setAlarmHour(is24HourFormat, hourNumberPicker.value)
            setAlarmMinute(minuteNumberPicker.value)
            negativeAlarmButton.visibility = View.GONE
        }

        // Notify the LiveData so that it updates the time remaining
        createEditAlarmViewModel.timeChangedAlarmLiveData.value = alarm

        positiveAlarmButton.setOnClickListener {
            // This is a creation
            if (BaseModel.NO_ID == alarm?.id) {
                createAndScheduleAlarm()
            } else { // This is an update
                editAndScheduleAlarm()
            }

            dismiss()
        }

        negativeAlarmButton.setOnClickListener {
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
            closeMusicMenu()
            openSpotifyActivity()
        }

        defaultRingtoneButton.setOnClickListener {
            closeMusicMenu()
            selectDefaultRingtone()
        }

        moreOptionsButton.setOnClickListener {
            showMoreOptionsFragment()
        }
    }

    private fun showMoreOptionsFragment() {
        var fragment =
            childFragmentManager.findFragmentByTag(SettingsFragment.TAG) as MoreOptionsAlarmFragment?
        if (fragment == null) {
            fragment = MoreOptionsAlarmFragment.newInstance(getString(R.string.more_options))
            fragment.show(childFragmentManager, MoreOptionsAlarmFragment.TAG)
        }
    }

    private fun selectDefaultRingtone() {
        alarm?.let {
            alarm = Alarm(it)

            // TODO
            alarm!!.uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()

            createEditAlarmViewModel.alarmMetadataLiveData.value = alarm
        }
    }

    private fun openMusicMenu() {
        val spotifyAnimator = AnimatorUtils.getTranslationAnimatorSet(
            spotifyButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            0,
            400
        )

        val defaultAnimator = AnimatorUtils.getTranslationAnimatorSet(
            defaultRingtoneButton,
            true,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_BOTTOM_TO_TOP,
            true,
            100,
            400
        )

        spotifyAnimator.playTogether(defaultAnimator)
        spotifyAnimator.start()

        chooseRingtoneButton.isExpanded = true
        chooseRingtoneButton.setImageResource(R.drawable.ic_close)
    }

    private fun closeMusicMenu() {
        val spotifyAnimator = AnimatorUtils.getTranslationAnimatorSet(
            spotifyButton,
            false,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_TOP_TO_BOTTOM,
            true,
            0,
            400
        )

        val defaultAnimator = AnimatorUtils.getTranslationAnimatorSet(
            defaultRingtoneButton,
            false,
            AnimatorUtils.TranslationAxis.VERTICAL,
            AnimatorUtils.TranslationDirection.FROM_TOP_TO_BOTTOM,
            true,
            100,
            400
        )

        spotifyAnimator.playTogether(defaultAnimator)
        spotifyAnimator.start()

        chooseRingtoneButton.isExpanded = false
        chooseRingtoneButton.setImageResource(R.drawable.ic_music_note)
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

    private fun openSpotifyActivity() {
        val intent = Intent(context, SpotifyActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SPOTIFY_GET_PLAYLIST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPOTIFY_GET_PLAYLIST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val selectedPlaylist = data?.extras
                        ?.getParcelable(Arguments.ARGS_SPOTIFY_SELECTED_PLAYLIST) as PlaylistSimple?

                    alarm?.let {
                        alarm = SpotifyAlarm(
                            it, selectedPlaylist?.name, "", selectedPlaylist?.images?.get(0)?.url
                        )
                        alarm!!.uri = selectedPlaylist?.uri

                        createEditAlarmViewModel.alarmMetadataLiveData.value = alarm
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "CreateEditAlarmFragment"

        private const val REQUEST_CODE_SPOTIFY_GET_PLAYLIST = 21

        fun newInstance(dialogTitle: String): CreateEditAlarmFragment {
            val newAlarm = Alarm(
                BaseModel.NO_ID,
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
                uri = null
            )

            return newInstance(dialogTitle, newAlarm)
        }

        fun newInstance(dialogTitle: String, alarm: AbstractAlarm): CreateEditAlarmFragment {
            val args = Bundle()

            args.putString(Arguments.ARGS_DIALOG_TITLE, dialogTitle)
            args.putParcelable(Arguments.ARGS_ALARM, alarm)

            val fragment = CreateEditAlarmFragment()
            fragment.arguments = args

            return fragment
        }
    }
}