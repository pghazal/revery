package com.pghaz.revery.player

import android.content.Context
import android.util.Log
import com.pghaz.revery.R
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track

class SpotifyPlayer : AbstractPlayer() {

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var connectionParams: ConnectionParams? = null

    private lateinit var currentUri: String

    private val spotifyConnector = object : Connector.ConnectionListener {
        override fun onConnected(sar: SpotifyAppRemote?) {
            Log.e(TAG, "Connected! Yay!")
            spotifyAppRemote = sar
            spotifyAppRemote?.playerApi?.setShuffle(true)

            onPlayerInitializedListener?.onPlayerInitialized()
        }

        override fun onFailure(error: Throwable?) {
            Log.e(TAG, error?.message, error)

            if (error is NotLoggedInException || error is UserNotAuthorizedException) {
                // Show login button and trigger the login flow from auth library when clicked
            } else if (error is CouldNotFindSpotifyApp) {
                // Show button to download Spotify
            }
        }
    }

    override fun init(context: Context) {
        connectionParams = ConnectionParams.Builder(context.getString(R.string.spotify_client_id))
            .setRedirectUri(context.getString(R.string.spotify_redirect_uri))
            .showAuthView(false)
            .build()
    }

    override fun prepare(context: Context, uri: String) {
        // "spotify:playlist:3H8dsoJvkH7lUkaQlUNjPJ"
        currentUri = uri
        SpotifyAppRemote.connect(context, connectionParams, spotifyConnector)
    }

    override fun play() {
        // Play a playlist
        spotifyAppRemote?.playerApi?.play(currentUri)

        // Subscribe to PlayerState
        spotifyAppRemote?.playerApi
            ?.subscribeToPlayerState()
            ?.setEventCallback { playerState: PlayerState ->
                val track: Track? = playerState.track
                if (track != null) {
                    Log.e(TAG, track.name.toString() + " by " + track.artist.name)
                }
            }
    }

    override fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    override fun release() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    companion object {
        private const val TAG = "SpotifyPlayer"
    }
}