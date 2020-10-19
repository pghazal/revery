package com.pghaz.revery.alarm.model.room

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pghaz.revery.alarm.repository.AlarmDatabase

@Entity(tableName = AlarmDatabase.ALARM_SPOTIFY_TABLE_NAME)
data class RSpotifyAlarm(

    @NonNull
    @PrimaryKey
    override var id: Long = 0,
    override var hour: Int = 0,
    override var minute: Int = 0,
    override var label: String = "",
    override var enabled: Boolean = true,
    override var recurring: Boolean = false,
    override var monday: Boolean = false,
    override var tuesday: Boolean = false,
    override var wednesday: Boolean = false,
    override var thursday: Boolean = false,
    override var friday: Boolean = false,
    override var saturday: Boolean = false,
    override var sunday: Boolean = false,
    override var vibrate: Boolean = false,
    override var fadeIn: Boolean = false,
    override var fadeInDuration: Long = 0,
    override var uri: String? = null,

    // RAlarmSpotify specifics
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null
) : RAbstractAlarm()