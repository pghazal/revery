package com.pghaz.revery.spotify.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pghaz.revery.alarm.model.BaseModel
import com.pghaz.revery.spotify.model.ArtistWrapper
import com.pghaz.revery.spotify.model.PlaylistWrapper
import com.pghaz.revery.spotify.model.TrackWrapper
import io.github.kaaes.spotify.webapi.core.Options
import io.github.kaaes.spotify.webapi.core.models.*
import io.github.kaaes.spotify.webapi.retrofit.v2.Spotify
import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyCallback
import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyError
import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyService
import retrofit2.Call
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class SpotifyItemsViewModel(accessToken: String?) : ViewModel() {

    val spotifyItemsLiveData = MutableLiveData<List<BaseModel>>()

    private val spotifyService: SpotifyService = Spotify.createAuthenticatedService(accessToken)

    private var mCurrentOffset = 0
    private var mPageSize = 0
    private var mBefore: String? = null

    fun getFirstPage() {
        mCurrentOffset = 0
        mPageSize = PAGE_SIZE
        getRecentlyPlayed(null, mPageSize)
    }

    fun getNextPage() {
        mCurrentOffset += mPageSize
        getRecentlyPlayed(mBefore, mPageSize)
    }

    private fun getRecentlyPlayed(before: String?, limit: Int) {
        val options: MutableMap<String, Any> = HashMap()
        options[Options.LIMIT] = limit

        if (!before.isNullOrEmpty()) {
            options[Options.BEFORE] = before
        }

        val call = spotifyService.getRecentlyPlayed(options)

        call.enqueue(object : SpotifyCallback<CursorPager<RecentlyPlayedTrack>>() {
            override fun onResponse(
                call: Call<CursorPager<RecentlyPlayedTrack>>?,
                response: Response<CursorPager<RecentlyPlayedTrack>>?,
                payload: CursorPager<RecentlyPlayedTrack>?
            ) {
                mBefore = payload?.cursors?.before

                val newItems = ArrayList<BaseModel>()

                spotifyItemsLiveData.value?.let { newItems.addAll(it) }

                payload?.items?.forEach {
                    newItems.add(TrackWrapper(it.track))
                }

                spotifyItemsLiveData.value = newItems
            }

            override fun onFailure(
                call: Call<CursorPager<RecentlyPlayedTrack>>?,
                error: SpotifyError?
            ) {
                //listener.onError(error)
                Log.e(TAG, "getFeaturedPlaylists() failed: ${error?.message}")
            }
        })
    }

    private fun getFeaturedPlaylists(offset: Int, limit: Int) {
        val options: MutableMap<String, Any> = HashMap()
        options[Options.OFFSET] = offset
        options[Options.LIMIT] = limit

        val call = spotifyService.getFeaturedPlaylists(options)

        call.enqueue(object : SpotifyCallback<FeaturedPlaylists>() {
            override fun onResponse(
                call: Call<FeaturedPlaylists>?,
                response: Response<FeaturedPlaylists>?,
                payload: FeaturedPlaylists?
            ) {
                val newItems = ArrayList<BaseModel>()

                spotifyItemsLiveData.value?.let { newItems.addAll(it) }

                payload?.playlists?.items?.forEach {
                    newItems.add(PlaylistWrapper(it))
                }

                spotifyItemsLiveData.value = newItems
            }

            override fun onFailure(call: Call<FeaturedPlaylists>?, error: SpotifyError?) {
                //listener.onError(error)
                Log.e(TAG, "getFeaturedPlaylists() failed: ${error?.message}")
            }
        })
    }

    private fun getMyTopArtists(offset: Int, limit: Int) {
        val options: MutableMap<String, Any> = HashMap()
        options[Options.OFFSET] = offset
        options[Options.LIMIT] = limit

        val call = spotifyService.getTopArtists(options)

        call.enqueue(object : SpotifyCallback<Pager<Artist>>() {
            override fun onResponse(
                call: Call<Pager<Artist>>?, response: Response<Pager<Artist>>?,
                payload: Pager<Artist>?
            ) {
                val newItems = ArrayList<BaseModel>()

                spotifyItemsLiveData.value?.let { newItems.addAll(it) }

                payload?.items?.forEach {
                    newItems.add(ArtistWrapper(it))
                }

                spotifyItemsLiveData.value = newItems
            }

            override fun onFailure(call: Call<Pager<Artist>>?, error: SpotifyError?) {
                //listener.onError(error)
                Log.e(TAG, "getTopArtists() failed: ${error?.message}")
            }
        })
    }

    private fun getMyPlaylists(offset: Int, limit: Int) {
        val options: MutableMap<String, Any> = HashMap()
        options[Options.OFFSET] = offset
        options[Options.LIMIT] = limit

        val call = spotifyService.getMyPlaylists(options)

        call.enqueue(object : SpotifyCallback<Pager<PlaylistSimple>>() {
            override fun onResponse(
                call: Call<Pager<PlaylistSimple>>?, response: Response<Pager<PlaylistSimple>>?,
                payload: Pager<PlaylistSimple>?
            ) {
                val newItems = ArrayList<BaseModel>()

                spotifyItemsLiveData.value?.let { newItems.addAll(it) }

                payload?.items?.forEach {
                    newItems.add(PlaylistWrapper(it))
                }

                spotifyItemsLiveData.value = newItems
            }

            override fun onFailure(call: Call<Pager<PlaylistSimple>>?, error: SpotifyError?) {
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