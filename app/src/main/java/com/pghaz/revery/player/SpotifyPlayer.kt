package com.pghaz.revery.player

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.pghaz.revery.R
import com.pghaz.revery.extension.logError
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class SpotifyPlayer(context: Context, shouldUseDeviceVolume: Boolean) :
    AbstractPlayer(context, AudioManager.STREAM_MUSIC, shouldUseDeviceVolume),
    Connector.ConnectionListener {

    private var connectionParams: ConnectionParams? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null

    private lateinit var currentUri: String

    override fun init() {
        connectionParams = ConnectionParams.Builder(context.getString(R.string.spotify_client_id))
            .setRedirectUri(context.getString(R.string.spotify_redirect_uri))
            .showAuthView(false)
            .build()
    }

    override fun prepare(uri: String) {
        // "spotify:playlist:3H8dsoJvkH7lUkaQlUNjPJ"
        currentUri = uri
        SpotifyAppRemote.connect(context, connectionParams, this)
    }

    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
        this.spotifyAppRemote = spotifyAppRemote

        this.spotifyAppRemote?.playerApi?.setShuffle(true) // TODO: settings

        onPlayerInitializedListener?.onPlayerInitialized()
    }

    override fun onFailure(error: Throwable?) {
        context.logError(error?.message, error)

        if (error is NotLoggedInException || error is UserNotAuthorizedException) {
            // Show login button and trigger the login flow from auth library when clicked
        } else if (error is CouldNotFindSpotifyApp) {
            // Show button to download Spotify
        }
    }

    override fun play() {
        // If fade in enabled, first set minimum volume
        if (fadeIn) {
            initFadeIn()
        }

        getAppRemote()?.playerApi?.play(currentUri)

        if (fadeIn) {
            fadeIn()
        }

        // Subscribe to PlayerState
        getAppRemote()?.playerApi
            ?.subscribeToPlayerState()
            ?.setEventCallback { playerState: PlayerState ->
                val track: Track? = playerState.track
                if (track != null) {
                    context.logError(track.name.toString() + " by " + track.artist.name)
                }
            }
    }

    override fun pause() {
        getAppRemote()?.playerApi?.pause()

        if (fadeIn) {
            resetVolumeFromFadeIn()
        }
    }

    override fun release() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    private fun getAppRemote(): SpotifyAppRemote? {
        return spotifyAppRemote
    }
}