package com.pghaz.revery.timer

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.pghaz.revery.BaseCreateEditFragment
import com.pghaz.revery.R
import com.pghaz.revery.alarm.MoreOptionsAlarmFragment
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.MediaType
import com.pghaz.revery.model.app.Timer
import com.pghaz.revery.ringtone.AudioPickerHelper
import com.pghaz.revery.settings.SettingsFragment
import com.pghaz.revery.settings.SettingsHandler
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.viewmodel.timer.CreateEditTimerViewModel
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.fragment_timer_create_edit.*

class CreateEditTimerFragment : BaseCreateEditFragment() {

    private lateinit var createEditTimerViewModel: CreateEditTimerViewModel
    private var timer: Timer? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_timer_create_edit
    }

    override fun parseArguments(arguments: Bundle?) {
        super.parseArguments(arguments)
        arguments?.let {
            timer = it.getParcelable(Arguments.ARGS_TIMER)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Arguments.ARGS_TIMER, timer)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createEditTimerViewModel = ViewModelProvider(this).get(CreateEditTimerViewModel::class.java)
        createEditTimerViewModel.timeChangedTimerLiveData.observe(this, { updatedTimer ->
            // TODO ?
            /*val timeRemainingInfo = DateTimeUtils.getTimeRemaining(updatedTimer)
            timeRemainingTextView.text =
                DateTimeUtils.getRemainingTimeText(timeRemainingTextView.context, timeRemainingInfo)*/
        })

        createEditTimerViewModel.metadataLiveData.observe(this, { metadata ->
            timer?.metadata = metadata
            updateMetadataViews(metadata)
        })
    }

    private fun initTimePicker() {
        context?.let {
            val face = ResourcesCompat.getFont(it, R.font.montserrat_regular)
            hourNumberPicker.setSelectedTypeface(face)
            minuteNumberPicker.setSelectedTypeface(face)
            hourNumberPicker.typeface = face
            minuteNumberPicker.typeface = face
            hourNumberPicker.formatter = NumberPicker.getTwoDigitFormatter()
            minuteNumberPicker.formatter = NumberPicker.getTwoDigitFormatter()

            hourNumberPicker.maxValue = it.resources.getInteger(R.integer.format_24_hour_max)
            hourNumberPicker.minValue = it.resources.getInteger(R.integer.format_24_hour_min)
        }
    }

    private fun configureTimePickerNow() {

    }

    private fun configureTimePickerFromTimer(timer: Timer) {
        // TODO
        /*val alarmHour = timer.hour

        hourNumberPicker.value = if (is24HourFormat) {
            alarmHour
        } else {
            DateTimeUtils.get12HourFormatFrom24HourFormat(alarmHour)
        }
        minuteNumberPicker.value = timer.minute

        amRadioButton.isChecked = DateTimeUtils.isAM(alarmHour)
        pmRadioButton.isChecked = !DateTimeUtils.isAM(alarmHour)*/
    }

    private fun configureViewsFromTimer(timer: Timer) {
        labelEditText.setText(timer.label)

        vibrateToggle.isChecked = timer.vibrate
        // TODO rename fadeIn
        fadeInToggle.isChecked = timer.fadeOut

        createEditTimerViewModel.metadataLiveData.value = timer?.metadata
    }

    private fun setTimerHour(hour: Int) {
        // TODO

        /*timer?.hour = if (is24HourFormat) {
            hour
        } else {
            DateTimeUtils.get24HourFormatFrom12HourFormat(
                hour,
                amRadioButton.isChecked
            )
        }*/
    }

    private fun setTimerMinute(minute: Int) {
        // TODO 
        //timer?.minute = minute
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)

        initTimePicker()

        // If we're editing an alarm, we set time/minute on the time picker
        if (timer?.id != BaseModel.NO_ID) {
            configureTimePickerFromTimer(timer!!)
            // Set views data before any listeners are set
            configureViewsFromTimer(timer!!)
            // Also show negative button (delete button)
            negativeButton.visibility = View.VISIBLE
        } else {
            configureTimePickerNow()
            setTimerHour(hourNumberPicker.value)
            setTimerMinute(minuteNumberPicker.value)
            negativeButton.visibility = View.GONE
        }

        // Notify the LiveData so that it updates the time remaining
        createEditTimerViewModel.timeChangedTimerLiveData.value = timer

        positiveButton.setOnClickListener {
            // This is a creation
            if (BaseModel.NO_ID == timer?.id) {
                createAndScheduleTimer()
            } else { // This is an update
                editAndScheduleTimer()
            }

            dismiss()
        }

        negativeButton.setOnClickListener {
            // If we were creating an alarm and click the negative button, just dismiss the dialog
            if (BaseModel.NO_ID == timer?.id) {
                // do nothing
            } else { // otherwise delete the existing alarm
                createEditTimerViewModel.delete(context, timer!!)
            }

            dismiss()
        }

        hourNumberPicker.setOnValueChangedListener { _, _, hour ->
            setTimerHour(hour)
            createEditTimerViewModel.timeChangedTimerLiveData.value = timer
        }

        minuteNumberPicker.setOnValueChangedListener { _, _, minute ->
            setTimerMinute(minute)
            createEditTimerViewModel.timeChangedTimerLiveData.value = timer
        }

        vibrateToggle.setOnCheckedChangeListener { _, _ ->
            timer?.vibrate = vibrateToggle.isChecked
        }

        fadeInToggle.setOnCheckedChangeListener { _, _ ->
            timer?.fadeOut = fadeInToggle.isChecked
            // This is not really useful for now because all alarms have same fade in duration
            //timer.fadeOutDuration = SettingsHandler.getFadeInDuration(buttonView.context)
        }

        moreOptionsButton.setOnClickListener {
            showMoreOptionsFragment()
        }
    }

    private fun showMoreOptionsFragment() {
        var fragment =
            childFragmentManager.findFragmentByTag(SettingsFragment.TAG) as MoreOptionsAlarmFragment?
        if (fragment == null) {
            // TODO
            /*fragment = MoreOptionsAlarmFragment.newInstance(
                getString(R.string.more_options),
                timer
            )
            fragment.show(childFragmentManager, MoreOptionsAlarmFragment.TAG)*/
        }
    }

    private fun createAndScheduleTimer() {
        val label: String =
            if (TextUtils.isEmpty(labelEditText.text?.trim())) "" else labelEditText.text?.trim()
                .toString()

        timer?.let {
            it.id = System.currentTimeMillis()
            it.label = label
            it.enabled = true

            // Safe init of uri
            if (it.metadata.uri.isNullOrEmpty()) {
                it.metadata = MediaMetadata().apply {
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

            createEditTimerViewModel.createTimer(context, it)
        }
    }

    private fun editAndScheduleTimer() {
        val label: String =
            if (TextUtils.isEmpty(labelEditText.text?.trim())) "" else labelEditText.text?.trim()
                .toString()

        timer?.let {
            it.label = label
            it.enabled = true

            createEditTimerViewModel.editTimer(context, it)
        }
    }

    override fun handleDefaultRingtoneClicked(metadata: MediaMetadata) {
        createEditTimerViewModel.metadataLiveData.value = metadata
    }

    override fun handleMusicSelection(metadata: MediaMetadata) {
        createEditTimerViewModel.metadataLiveData.value = metadata
    }

    override fun handleRingtoneSelection(metadata: MediaMetadata) {
        createEditTimerViewModel.metadataLiveData.value = metadata
    }

    override fun handleSpotifySelection(metadata: MediaMetadata) {
        timer?.let {
            metadata.apply {
                this.shuffle = it.metadata.shuffle
                this.shouldKeepPlaying = it.metadata.shouldKeepPlaying
                this.repeat = it.metadata.repeat
            }

            createEditTimerViewModel.metadataLiveData.value = metadata
        }
    }

    companion object {
        const val TAG = "CreateEditTimerFragment"

        fun newInstance(dialogTitle: String): CreateEditTimerFragment {
            val newTimer = Timer(
                id = BaseModel.NO_ID, // Let this ´NO_ID´ value. It defines if it's creation or edition of timer
                durationInSeconds = 0,
                label = "",
                enabled = true,
                vibrate = false,
                fadeOut = false,
                fadeOutDuration = 0,
                metadata = MediaMetadata()
            )

            return newInstance(dialogTitle, newTimer)
        }

        fun newInstance(
            dialogTitle: String,
            timer: Timer
        ): CreateEditTimerFragment {
            val args = Bundle()

            args.putString(Arguments.ARGS_DIALOG_TITLE, dialogTitle)
            args.putParcelable(Arguments.ARGS_TIMER, timer)

            val fragment = CreateEditTimerFragment()
            fragment.arguments = args

            return fragment
        }
    }
}