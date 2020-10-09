package com.pghaz.revery.spotify.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.SpotifyCallback
import kaaes.spotify.webapi.android.SpotifyError
import kaaes.spotify.webapi.android.SpotifyService
import kaaes.spotify.webapi.android.models.Pager
import kaaes.spotify.webapi.android.models.PlaylistSimple
import retrofit.client.Response
import java.util.*

class SpotifyItemsViewModel(accessToken: String?) : ViewModel() {

    val spotifyItemsLiveData = MutableLiveData<List<PlaylistSimple>>()

    private val spotifyApi: SpotifyApi = SpotifyApi()
    private val spotifyService: SpotifyService = spotifyApi.service

    private var mCurrentOffset = 0
    private var mPageSize = 0

    init {
        if (accessToken != null) {
            spotifyApi.setAccessToken(accessToken)
        } else {
            Log.e(TAG, "Access token not valid")
        }
    }

    fun getFirstPage() {
        mCurrentOffset = 0
        mPageSize = PAGE_SIZE
        getMyPlaylists(0, mPageSize)
    }

    fun getNextPage() {
        mCurrentOffset += mPageSize
        getMyPlaylists(mCurrentOffset, mPageSize)
    }

    private fun getMyPlaylists(offset: Int, limit: Int) {
        val options: MutableMap<String, Any> = HashMap()
        options[SpotifyService.OFFSET] = offset
        options[SpotifyService.LIMIT] = limit

        spotifyService.getMyPlaylists(options, object : SpotifyCallback<Pager<PlaylistSimple>>() {
            override fun success(pager: Pager<PlaylistSimple>?, response: Response?) {
                spotifyItemsLiveData.value = pager?.items
            }

            override fun failure(error: SpotifyError?) {
                //listener.onError(error)
                Log.e(TAG, "getMyPlaylists() failed: ${error?.message}")
            }
        })
    }

    companion object {
        private const val TAG = "SpotifyItemsViewModel"
        private const val PAGE_SIZE = 20
    }
}