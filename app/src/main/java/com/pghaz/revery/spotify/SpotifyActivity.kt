package com.pghaz.revery.spotify

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R
import com.pghaz.revery.extension.toast
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationCallback
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationClient
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import net.openid.appauth.TokenResponse

class SpotifyActivity : BaseActivity(), SpotifyAuthorizationCallback.Authorize,
    SpotifyAuthorizationCallback.RefreshToken {

    companion object {
        private const val REQUEST_CODE_SPOTIFY_LOGIN = 1337
    }

    private lateinit var spotifyAuthClient: SpotifyAuthorizationClient

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

    private fun initSpotifyAuthClient() {
        spotifyAuthClient = SpotifyAuthorizationClient.Builder(
            getString(R.string.spotify_client_id),
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
            .setFetchUserAfterAuthorization(true)
            .setCustomTabColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .build(this)

        spotifyAuthClient.setDebugMode(BuildConfig.DEBUG)
        spotifyAuthClient.setAuthorizationCallback(this)
        spotifyAuthClient.setRefreshTokenCallback(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initSpotifyAuthClient()

        if (spotifyAuthClient.isAuthorized()) {
            if (spotifyAuthClient.getNeedsTokenRefresh()) {
                spotifyAuthClient.refreshAccessToken()
            } else {
                // We already have an access token, we can display user's playlist
                showPlaylistsFragment(spotifyAuthClient.getLastTokenResponse()?.accessToken!!)
            }
        } else {
            spotifyAuthClient.authorize(this, REQUEST_CODE_SPOTIFY_LOGIN)
        }
    }

    override fun parseArguments(args: Bundle?) {
        // do nothing
    }

    override fun onStart() {
        super.onStart()
        spotifyAuthClient.onStart()
    }

    override fun onStop() {
        super.onStop()
        spotifyAuthClient.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        spotifyAuthClient.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // At this point it is not yet authorized. See onAuthorizationSucceed()
        spotifyAuthClient.onActivityResult(requestCode, resultCode, data)
    }

    private fun showPlaylistsFragment(accessToken: String) {
        replaceFragment(
            SpotifyPlaylistsFragment.newInstance(accessToken),
            SpotifyPlaylistsFragment.TAG
        )
    }

    override fun onAuthorizationCanceled() {
        finish()
    }

    override fun onAuthorizationFailed(error: String?) {
        toast("Failed")
    }

    override fun onAuthorizationRefused(error: String?) {
        toast("Refused")
    }

    override fun onAuthorizationStarted() {
        toast("Authorization starting")
    }

    override fun onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        showPlaylistsFragment(spotifyAuthClient.getLastTokenResponse()?.accessToken!!)
    }

    override fun onRefreshAccessTokenStarted() {
        toast("Getting new Access Token")
    }

    override fun onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        showPlaylistsFragment(spotifyAuthClient.getLastTokenResponse()?.accessToken!!)
    }
}