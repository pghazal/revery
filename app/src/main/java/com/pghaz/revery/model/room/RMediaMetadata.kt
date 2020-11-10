package com.pghaz.revery.model.room

import androidx.room.TypeConverters
import com.pghaz.revery.repository.converter.MediaTypeConverters

@TypeConverters(MediaTypeConverters::class)
data class RMediaMetadata(
    var uri: String? = null,
    var href: String? = null,
    var type: RMediaType = RMediaType.DEFAULT,
    var name: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var shuffle: Boolean = false,
    var shouldKeepPlaying: Boolean = false,
    var repeat: Int = 0
) : RBaseModel()