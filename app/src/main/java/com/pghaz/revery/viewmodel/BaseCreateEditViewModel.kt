package com.pghaz.revery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pghaz.revery.model.app.MediaMetadata

abstract class BaseCreateEditViewModel(application: Application) : AndroidViewModel(application) {

    val metadataLiveData = MutableLiveData<MediaMetadata>()
}