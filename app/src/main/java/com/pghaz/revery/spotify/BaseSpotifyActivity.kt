package com.pghaz.revery.spotify

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.pghaz.revery.BaseActivity
import com.pghaz.revery.BuildConfig
import com.pghaz.revery.R
import com.pghaz.revery.extension.toastDebug
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationCallback
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationClient
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import net.openid.appauth.TokenResponse

abstract class BaseSpotifyActivity : BaseActivity(), SpotifyAuthorizationCallback.Authorize,
    SpotifyAuthorizationCallback.RefreshToken {

    companion object {
        private const val REQUEST_CODE_SPOTIFY_LOGIN = 1337
    }

    protected lateinit var spotifyAuthClient: SpotifyAuthorizationClient

    private fun initSpotifyAuthClient() {
        spotifyAuthClient = SpotifyAuthorizationClient.Builder(
            BuildConfig.SPOTIFY_CLIENT_ID,
            BuildConfig.SPOTIFY_REDIRECT_URI
        )
            .setScopes(
                arrayOf(
                    "app-remote-control",
                    "playlist-read-private",
                    "playlist-read-collaborative",
                    "user-top-read",
                    "user-read-private",
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initSpotifyAuthClient()

        if (spotifyAuthClient.isAuthorized()) {
            if (spotifyAuthClient.getNeedsTokenRefresh()) {
                spotifyAuthClient.refreshAccessToken()
            } else {
                onSpotifyAuthorizedAndAvailable()
            }
        } else {
            spotifyAuthClient.authorize(this, REQUEST_CODE_SPOTIFY_LOGIN)
        }
    }

    abstract fun onSpotifyAuthorizedAndAvailable()

    override fun onStart() {
        super.onStart()
        spotifyAuthClient.onStart()
    }

    override fun onResume() {
        super.onResume()

        if (!SpotifyAuthorizationClient.isSpotifyInstalled(this)) {
            showSpotifyNotInstalledDialog()
        }
    }

    private fun showSpotifyNotInstalledDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_spotify_not_installed, null)
        val spotifyInstallButton = view.findViewById<AppCompatButton>(R.id.spotifyInstallButton)

        val dialog = AlertDialog.Builder(this).apply {
            setCancelable(true)
            setView(view)
        }.create()
        dialog.show()

        spotifyInstallButton.setOnClickListener {
            dialog.dismiss()
            SpotifyAuthorizationClient.openDownloadSpotifyActivity(this, "com.pghaz.revery")
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return false
    }

    override fun onAuthorizationCanceled() {
        finish()
    }

    override fun onAuthorizationFailed(error: String?) {
        toastDebug("Failed")
        finish()
    }

    override fun onAuthorizationRefused(error: String?) {
        toastDebug("Refused")
        finish()
    }

    override fun onAuthorizationStarted() {
        toastDebug("Authorization starting")
    }

    override fun onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        onSpotifyAuthorizedAndAvailable()
    }

    override fun onRefreshAccessTokenStarted() {
        toastDebug("Getting new Access Token")
    }

    override fun onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        onSpotifyAuthorizedAndAvailable()
    }
}