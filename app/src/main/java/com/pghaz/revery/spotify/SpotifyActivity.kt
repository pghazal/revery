package com.pghaz.revery.spotify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R
import com.pghaz.revery.spotify.util.CredentialsHandler
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.util.concurrent.TimeUnit

class SpotifyActivity : BaseActivity() {

    companion object {
        private const val TAG = "SpotifyActivity"

        private const val CLIENT_ID = "7cb7f4b08ff748cc9f19c84cc627f9d9"
        private const val REDIRECT_URI = "com.pghaz.revery://connect"
        private const val REQUEST_CODE_SPOTIFY_LOGIN = 1337
    }

    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var spotifyAuthorizationState: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_spotify
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        // do nothing yet
    }

    override fun shouldAnimateOnCreate(): Boolean {
        return true
    }

    override fun shouldAnimateOnFinish(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SpotifyAppRemote.setDebugMode(BuildConfig.DEBUG)

        // TODO: get new access token ? with renew ?
        val accessToken = CredentialsHandler.getToken(this)
        if (accessToken == null) {
            authorizeSpotify()
        } else {
            // We already have an access token, we can display user's playlist
            showPlaylistsFragment(accessToken)
        }
    }

    // Token will be received in onActivityForResult()
    private fun authorizeSpotify() {
        spotifyAuthorizationState = System.currentTimeMillis().toString()

        val request: AuthorizationRequest =
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(
                    arrayOf(
                        "app-remote-control",
                        "playlist-read-private",
                        "playlist-read-collaborative"
                    )
                )
                .setShowDialog(false)
                .setState(spotifyAuthorizationState)
                .build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE_SPOTIFY_LOGIN, request)
    }

    private fun handleSpotifyAuthorizationResponse(resultCode: Int, data: Intent?): String? {
        val response: AuthorizationResponse =
            AuthorizationClient.getResponse(resultCode, data)

        // This is a check for security reason, in case of man-in-the-middle
        if (response.state == spotifyAuthorizationState) {
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // Save TOKEN into SharedPrefs
                    CredentialsHandler.setToken(
                        this,
                        response.accessToken,
                        response.expiresIn,
                        TimeUnit.SECONDS
                    )
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, "Auth error : " + response.error)
                }

                else -> {
                    Log.e(TAG, "Auth result: " + response.type)
                }
            }
        }

        return response.accessToken
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPOTIFY_LOGIN) {
            when (resultCode) {
                RESULT_OK -> {
                    val accessToken = handleSpotifyAuthorizationResponse(resultCode, data)
                    if (accessToken != null) {
                        showPlaylistsFragment(accessToken)
                    }
                }

                RESULT_CANCELED -> {
                    // handle
                }
            }
        }
    }

    private fun showPlaylistsFragment(accessToken: String) {
        replaceFragment(
            SpotifyPlaylistsFragment.newInstance(accessToken),
            SpotifyPlaylistsFragment.TAG
        )
    }

    private fun connectToSpotifyAppRemote() {
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            this,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote?) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.e(TAG, "Connected! Yay!")

                    // Now you can start interacting with App Remote
                    //connected()
                }

                override fun onFailure(error: Throwable?) {
                    Log.e(TAG, error?.message, error)

                    if (error is NotLoggedInException || error is UserNotAuthorizedException) {
                        // Show login button and trigger the login flow from auth library when clicked
                    } else if (error is CouldNotFindSpotifyApp) {
                        // Show button to download Spotify
                    }
                }
            })
    }

    private fun connected() {
        // Play a playlist
        mSpotifyAppRemote?.playerApi?.play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL")

        // Subscribe to PlayerState
        mSpotifyAppRemote?.playerApi
            ?.subscribeToPlayerState()
            ?.setEventCallback { playerState: PlayerState ->
                val track: Track? = playerState.track
                if (track != null) {
                    Log.e(TAG, track.name.toString() + " by " + track.artist.name)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }
}