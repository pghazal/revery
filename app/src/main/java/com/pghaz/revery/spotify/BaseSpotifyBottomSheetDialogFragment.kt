package com.pghaz.revery.spotify

import android.content.Context
import android.widget.Toast
import com.pghaz.revery.BaseBottomSheetDialogFragment
import com.pghaz.revery.R
import com.pghaz.revery.viewmodel.spotify.SpotifyErrorListener
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationCallback
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyError
import net.openid.appauth.TokenResponse

abstract class BaseSpotifyBottomSheetDialogFragment : BaseBottomSheetDialogFragment(),
    SpotifyAuthorizationCallback.Authorize,
    SpotifyAuthorizationCallback.RefreshToken,
    SpotifyErrorListener {

    abstract fun onSpotifyAuthorizedAndAvailable()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is BaseSpotifyActivity) {
            context.spotifyAuthClient.addAuthorizationCallback(this)
            context.spotifyAuthClient.addRefreshTokenCallback(this)
        }
    }

    override fun onDetach() {
        super.onDetach()

        if (activity is BaseSpotifyActivity) {
            (activity as BaseSpotifyActivity).spotifyAuthClient.removeAuthorizationCallback(this)
            (activity as BaseSpotifyActivity).spotifyAuthClient.removeRefreshTokenCallback(this)
        }
    }

    override fun onSpotifyError(error: SpotifyError) {
        val errorMessage = when (error.details.status) {
            SpotifyError.ERROR_NETWORK -> getString(R.string.error_network)
            else -> getString(R.string.error_unexpected)
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onAuthorizationCancelled() {

    }

    override fun onAuthorizationFailed(error: String?) {

    }

    override fun onAuthorizationRefused(error: String?) {

    }

    override fun onAuthorizationStarted() {

    }

    override fun onAuthorizationSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        onSpotifyAuthorizedAndAvailable()
    }

    override fun onRefreshAccessTokenStarted() {

    }

    override fun onRefreshAccessTokenSucceed(tokenResponse: TokenResponse?, user: UserPrivate?) {
        onSpotifyAuthorizedAndAvailable()
    }
}