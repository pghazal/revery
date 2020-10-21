package com.pghaz.revery.spotify

import android.content.Intent
import android.os.Bundle
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R
import com.pghaz.revery.extension.logError
import com.pghaz.revery.spotify.util.CredentialsHandler
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.util.concurrent.TimeUnit

class SpotifyActivity : BaseActivity() {

    companion object {
        private const val REQUEST_CODE_SPOTIFY_LOGIN = 1337
    }

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

        val accessToken = CredentialsHandler.getToken(this)
        if (accessToken == null) {
            authorizeSpotify()
        } else {
            // We already have an access token, we can display user's playlist
            showPlaylistsFragment(accessToken)
        }
    }

    override fun parseArguments(args: Bundle?) {
        // do nothing
    }

    // Token will be received in onActivityForResult()
    private fun authorizeSpotify() {
        spotifyAuthorizationState = System.currentTimeMillis().toString()

        val request: AuthorizationRequest =
            AuthorizationRequest.Builder(
                getString(R.string.spotify_client_id),
                AuthorizationResponse.Type.TOKEN,
                getString(R.string.spotify_redirect_uri)
            )
                .setScopes(
                    arrayOf(
                        "app-remote-control",
                        "playlist-read-private",
                        "playlist-read-collaborative",
                        "user-top-read",
                        "user-read-recently-played" // TODO
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
                    logError("Auth error : " + response.error)
                }

                else -> {
                    logError("Auth result: " + response.type)
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
                    finish()
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
}