package com.pghaz.revery.player

import android.content.Context
import android.media.AudioManager
import com.pghaz.revery.R
import com.pghaz.revery.extension.logError
import com.pghaz.revery.spotify.util.ConnectionState
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.SpotifyConnectionTerminatedException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import kotlinx.coroutines.*

class SpotifyPlayer(context: Context, shouldUseDeviceVolume: Boolean) :
    AbstractPlayer(context, AudioManager.STREAM_MUSIC, shouldUseDeviceVolume),
    Connector.ConnectionListener {

    private var connectionParams: ConnectionParams? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null

    private var isInitialized = false
    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    private val job = Job()
    private val coroutinesScope: CoroutineScope = CoroutineScope(job + Dispatchers.Main)
    private val connectionStateCallbacks = mutableListOf<ConnectionStateCallback>()

    var shuffle: Boolean = false

    override fun init(playerListener: PlayerListener?) {
        super.init(playerListener)

        connectionParams = ConnectionParams.Builder(context.getString(R.string.spotify_client_id))
            .setRedirectUri(context.getString(R.string.spotify_redirect_uri))
            .showAuthView(false)
            .build()
    }

    override fun prepareAsync(uri: String?) {
        // "spotify:playlist:3H8dsoJvkH7lUkaQlUNjPJ"
        currentUri = uri!!
        SpotifyAppRemote.connect(context, connectionParams, this)
    }

    override fun prepare(uri: String?) {
        // do nothing
    }

    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
        this.spotifyAppRemote = spotifyAppRemote
        this.isInitialized = true
        this.connectionState = ConnectionState.CONNECTED

        this.spotifyAppRemote?.playerApi?.setShuffle(shuffle)

        playerListener?.onPlayerInitialized()

        callSuspendFunctionStateCallbacks(ConnectionState.CONNECTED)
    }

    override fun onFailure(error: Throwable?) {
        context.logError(error?.message, error)

        connectionState = ConnectionState.DISCONNECTED

        if (error is NotLoggedInException || error is UserNotAuthorizedException) {
            // Show login button and trigger the login flow from auth library when clicked
            playerListener?.onPlayerError(PlayerError.SpotifyPlayerUserNotAuthorized(error))
        } else if (error is CouldNotFindSpotifyApp) {
            // Show button to download Spotify
            playerListener?.onPlayerError(PlayerError.SpotifyPlayerNotInstalled(error))
        } else if (error is SpotifyConnectionTerminatedException) {
            SpotifyAppRemote.connect(context, connectionParams, this)
        } else {
            callSuspendFunctionStateCallbacks(ConnectionState.DISCONNECTED)
        }
    }

    @ExperimentalCoroutinesApi
    override fun play() {
        context.logError("play()")
        coroutinesScope.launch {
            context.logError("coroutinesScope.launch -> play()")
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
    }

    @ExperimentalCoroutinesApi
    override fun pause() {
        context.logError("pause()")
        coroutinesScope.launch {
            context.logError("coroutinesScope.launch -> pause()")
            getAppRemote()?.playerApi?.pause()

            if (fadeIn) {
                resetVolumeFromFadeIn()
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun release() {
        context.logError("release()")
        coroutinesScope.launch {
            context.logError("coroutinesScope.launch -> release()")
            SpotifyAppRemote.disconnect(getAppRemote())

            spotifyAppRemote = null
            job.cancel()
        }
    }

    private fun callSuspendFunctionStateCallbacks(connectionState: ConnectionState) {
        val callbacksIterator = connectionStateCallbacks.iterator()

        while (callbacksIterator.hasNext()) {
            val callback = callbacksIterator.next()
            callbacksIterator.remove()
            callback.onResult(connectionState)
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun getAppRemote(): SpotifyAppRemote? {
        if (!isInitialized) {
            throw IllegalStateException("getAppRemote() -> Spotify not initialized")
        }

        if (connectionState == ConnectionState.CONNECTED) {
            context.logError("getAppRemote() -> CONNECTED: Cached App Remote")
            return spotifyAppRemote
        }

        return suspendCancellableCoroutine { continuation ->
            context.logError("getAppRemote() -> Reconnect and suspend")

            val callback = object : ConnectionStateCallback {
                override fun onResult(connectionState: ConnectionState) {
                    when (connectionState) {
                        ConnectionState.CONNECTED -> {
                            continuation.resume(spotifyAppRemote) { cause: Throwable ->
                                context.logError("callback CONNECTED -> resume cancelled")
                                cause.printStackTrace()
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            context.logError("callback DISCONNECTED -> cancel()")
                            val throwable = Throwable(this@SpotifyPlayer.toString())
                            playerListener?.onPlayerError(PlayerError.SpotifyPlayerUnknown(throwable))
                            continuation.cancel()
                        }
                    }
                }
            }

            context.logError("Add ConnectionStateCallback to the list")
            connectionStateCallbacks.add(callback)
        }
    }

    override fun toString(): String {
        return "SpotifyPlayer(isInitialized=$isInitialized," +
                " connectionState=$connectionState)" +
                " ${super.toString()}"
    }

    interface ConnectionStateCallback {
        fun onResult(connectionState: ConnectionState)
    }
}
