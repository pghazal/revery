package com.pghaz.revery.player

import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.extension.logError
import com.spotify.android.appremote.api.*
import com.spotify.android.appremote.api.error.*
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Repeat
import kotlinx.coroutines.*

class SpotifyPlayer(context: Context, isEmergencyAlarm: Boolean, shouldUseDeviceVolume: Boolean) :
    AbstractPlayer(context, AudioManager.STREAM_MUSIC, isEmergencyAlarm, shouldUseDeviceVolume),
    Connector.ConnectionListener {

    private var connectionParams: ConnectionParams? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null

    private var isInitialized = false
    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    private var playerAction: PlayerAction = PlayerAction.ACTION_NONE

    private val job = Job()
    private val coroutinesScope: CoroutineScope = CoroutineScope(job + Dispatchers.Main)
    private val connectionStateCallbacks = mutableListOf<ConnectionStateCallback>()
    val playerConnectedLiveData = MutableLiveData<Boolean>()

    var shuffle: Boolean = false
    var repeat: Int = Repeat.OFF

    private var playerApi: PlayerApi? = null
    private var imagesApi: ImagesApi? = null

    override fun init(playerListener: PlayerListener?) {
        super.init(playerListener)
        playerConnectedLiveData.value = false

        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)

        connectionParams = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
            .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
            .showAuthView(false)
            .build()
    }

    // "spotify:playlist:3H8dsoJvkH7lUkaQlUNjPJ"
    override fun prepareAsync(uri: String?) {
        playerAction = PlayerAction.ACTION_START
        currentUri = uri!!
        SpotifyAppRemote.connect(context, connectionParams, this)

        FirebaseCrashlytics.getInstance().log("SpotifyPlayer.prepareAsync()")
    }

    override fun prepare(uri: String?) {
        playerAction = PlayerAction.ACTION_START
        currentUri = uri!!
        // sync prepared is not used for this player
    }

    @ExperimentalCoroutinesApi
    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
        this.spotifyAppRemote = spotifyAppRemote
        this.isInitialized = true
        this.connectionState = ConnectionState.CONNECTED
        this.spotifyAppRemote!!.playerApi.setShuffle(shuffle)
        this.spotifyAppRemote!!.playerApi.setRepeat(repeat)

        this.playerApi = this.spotifyAppRemote!!.playerApi
        this.imagesApi = this.spotifyAppRemote!!.imagesApi

        FirebaseCrashlytics.getInstance().log("SpotifyPlayer.onConnected($playerAction)")

        // When AppRemote disconnected and auto-reconnect, ´playerAction´ will exec unhandled action
        when (playerAction) {
            PlayerAction.ACTION_START -> playerListener?.onPlayerInitialized(this)
            PlayerAction.ACTION_STOP -> stop()
            PlayerAction.ACTION_PLAY -> play()
            PlayerAction.ACTION_PAUSE -> pause()
            PlayerAction.ACTION_RELEASE -> release()
            PlayerAction.ACTION_SKIP_NEXT -> skipNext()
            PlayerAction.ACTION_SKIP_PREVIOUS -> skipPrevious()
            else -> {
                // do nothing
            }
        }

        playerConnectedLiveData.value = true

        callSuspendFunctionStateCallbacks(ConnectionState.CONNECTED)
    }

    override fun onFailure(error: Throwable?) {
        context.logError(error?.stackTraceToString(), error)

        FirebaseCrashlytics.getInstance().log("SpotifyPlayer.onFailure()")

        connectionState = ConnectionState.DISCONNECTED

        playerConnectedLiveData.value = false

        if (error is SpotifyConnectionTerminatedException) {
            FirebaseCrashlytics.getInstance()
                .log("SpotifyPlayer.onFailure() : SpotifyConnectionTerminatedException")
            SpotifyAppRemote.connect(context, connectionParams, this)
        } else {
            val playerError = getPlayerError(error)
            playerListener?.onPlayerError(playerError)
            callSuspendFunctionStateCallbacks(ConnectionState.DISCONNECTED)
        }
    }

    private fun getPlayerError(error: Throwable?): SpotifyPlayerError {
        val message = this@SpotifyPlayer.toString()

        FirebaseCrashlytics.getInstance().log("SpotifyPlayer.getPlayerError()")

        return when (error) {
            is NotLoggedInException -> {
                SpotifyPlayerError.SpotifyNotLoggedIn(message, error)
            }

            is UserNotAuthorizedException -> {
                SpotifyPlayerError.SpotifyPlayerUserNotAuthorized(message, error)
            }

            is CouldNotFindSpotifyApp -> {
                SpotifyPlayerError.SpotifyPlayerNotInstalled(message, error)
            }

            is AuthenticationFailedException -> {
                SpotifyPlayerError.SpotifyAuthenticationFailed(message, error)
            }

            is UnsupportedFeatureVersionException -> {
                SpotifyPlayerError.SpotifyUnsupportedFeatureVersion(message, error)
            }

            is OfflineModeException -> {
                SpotifyPlayerError.SpotifyOfflineMode(message, error)
            }

            is SpotifyRemoteServiceException -> {
                SpotifyPlayerError.SpotifyRemoteService(message, error)
            }

            is SpotifyDisconnectedException -> {
                SpotifyPlayerError.SpotifyDisconnected(message, error)
            }

            else -> {
                SpotifyPlayerError.SpotifyPlayerUnknown(message, error)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun internalStart() {
        playerAction = PlayerAction.ACTION_START

        context.logError("internalStart()")
        coroutinesScope.launch {
            getAppRemote()?.playerApi?.play(currentUri)
        }
    }

    fun getPlayerStateSubscription(): Subscription<PlayerState>? {
        context.logError("getPlayerStateSubscription()")
        return playerApi?.subscribeToPlayerState()
    }

    fun getPlayerStateCallResult(): CallResult<PlayerState>? {
        context.logError("getPlayerStateCallResult()")
        return playerApi?.playerState
    }

    fun getImageUri(imageUri: ImageUri): CallResult<Bitmap>? {
        context.logError("getImageUri()")
        return imagesApi?.getImage(imageUri)
    }

    @ExperimentalCoroutinesApi
    override fun skipNext() {
        playerAction = PlayerAction.ACTION_SKIP_NEXT

        context.logError("skipNext()")
        coroutinesScope.launch {
            getAppRemote()?.playerApi?.skipNext()
        }
    }

    @ExperimentalCoroutinesApi
    override fun skipPrevious() {
        playerAction = PlayerAction.ACTION_SKIP_PREVIOUS

        context.logError("skipPrevious()")
        coroutinesScope.launch {
            getAppRemote()?.playerApi?.skipPrevious()
        }
    }

    @ExperimentalCoroutinesApi
    override fun stop() {
        playerAction = PlayerAction.ACTION_STOP

        context.logError("stop()")
        coroutinesScope.launch {
            FirebaseCrashlytics.getInstance().log("SpotifyPlayer.stop()")
            getAppRemote()?.playerApi?.pause()
        }
    }

    @ExperimentalCoroutinesApi
    override fun play() {
        playerAction = PlayerAction.ACTION_PLAY

        context.logError("play()")
        coroutinesScope.launch {
            FirebaseCrashlytics.getInstance().log("SpotifyPlayer.play()")
            getAppRemote()?.playerApi?.resume()
        }
    }

    @ExperimentalCoroutinesApi
    override fun pause() {
        playerAction = PlayerAction.ACTION_PAUSE

        context.logError("pause()")
        coroutinesScope.launch {
            FirebaseCrashlytics.getInstance().log("SpotifyPlayer.pause()")
            getAppRemote()?.playerApi?.pause()
        }
    }

    @ExperimentalCoroutinesApi
    override fun release() {
        playerAction = PlayerAction.ACTION_RELEASE

        context.logError("release()")
        coroutinesScope.launch {
            FirebaseCrashlytics.getInstance().log("SpotifyPlayer.release()")
            SpotifyAppRemote.disconnect(getAppRemote())

            // Give some time to the player to disconnect
            delay(500)

            resetInitialDeviceVolume()

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
        FirebaseCrashlytics.getInstance().log("SpotifyPlayer.getAppRemote()")
        if (!isInitialized) {
            FirebaseCrashlytics.getInstance().log("SpotifyPlayer.getAppRemote(): not initialized")
            SpotifyAppRemote.connect(context, connectionParams, this)
        }

        if (connectionState == ConnectionState.CONNECTED) {
            context.logError("getAppRemote() -> CONNECTED: Cached App Remote")
            FirebaseCrashlytics.getInstance().log("SpotifyPlayer.getAppRemote() connected")
            return spotifyAppRemote
        }

        return suspendCancellableCoroutine { continuation ->
            context.logError("getAppRemote() -> Reconnect and suspend")
            FirebaseCrashlytics.getInstance()
                .log("SpotifyPlayer.getAppRemote() Reconnect and suspend")

            val callback = object : ConnectionStateCallback {
                override fun onResult(connectionState: ConnectionState) {
                    when (connectionState) {
                        ConnectionState.CONNECTED -> {
                            FirebaseCrashlytics.getInstance()
                                .log("SpotifyPlayer.getAppRemote() reconnected")
                            continuation.resume(spotifyAppRemote) { cause: Throwable ->
                                context.logError("callback CONNECTED -> resume cancelled")
                                FirebaseCrashlytics.getInstance()
                                    .log("SpotifyPlayer.getAppRemote() resume cancelled")
                                cause.printStackTrace()
                            }
                        }
                        ConnectionState.DISCONNECTED -> {
                            context.logError("callback DISCONNECTED -> cancel()")
                            FirebaseCrashlytics.getInstance()
                                .log("SpotifyPlayer.getAppRemote() disconnected")
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
                " connectionState=$connectionState" +
                " playerAction=$playerAction)" +
                " ${super.toString()}"
    }

    interface ConnectionStateCallback {
        fun onResult(connectionState: ConnectionState)
    }
}
