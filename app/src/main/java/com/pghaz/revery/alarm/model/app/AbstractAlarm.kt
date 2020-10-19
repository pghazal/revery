package com.pghaz.revery.alarm.model.app

import com.pghaz.revery.alarm.model.BaseModel

abstract class AbstractAlarm : BaseModel() {

    abstract var id: Long
    abstract var hour: Int
    abstract var minute: Int
    abstract var label: String
    abstract var enabled: Boolean
    abstract var recurring: Boolean
    abstract var monday: Boolean
    abstract var tuesday: Boolean
    abstract var wednesday: Boolean
    abstract var thursday: Boolean
    abstract var friday: Boolean
    abstract var saturday: Boolean
    abstract var sunday: Boolean
    abstract var vibrate: Boolean
    abstract var fadeIn: Boolean
    abstract var fadeInDuration: Long
    abstract var uri: String?
}