package com.pghaz.revery.onboarding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.image.ImageLoader
import com.pghaz.revery.spotify.BaseSpotifyActivity
import com.pghaz.revery.spotify.BaseSpotifyFragment
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationClient
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import kotlinx.android.synthetic.main.fragment_on_boarding_spotify.*

class OnBoardingSpotifyFragment : BaseSpotifyFragment() {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_on_boarding_spotify
    }

    override fun onSpotifyAuthorizedAndAvailable() {
        updateSpotifyViews(
            (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.getCurrentUser(),
            true
        )
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        if (activity is BaseSpotifyActivity) {
            updateSpotifyViews(
                (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.getCurrentUser(),
                false
            )

            if (!SpotifyAuthorizationClient.isSpotifyInstalled(activity as BaseSpotifyActivity)) {
                spotifyNotInstalledContainer.visibility = View.VISIBLE
            } else {
                spotifyNotInstalledContainer.visibility = View.GONE
            }
            spotifyInstallButton.setOnClickListener {
                SpotifyAuthorizationClient.openDownloadSpotifyActivity(
                    activity as BaseSpotifyActivity,
                    "com.pghaz.revery"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!SpotifyAuthorizationClient.isSpotifyInstalled(activity as BaseSpotifyActivity)) {
            spotifyNotInstalledContainer.visibility = View.VISIBLE
        } else {
            spotifyNotInstalledContainer.visibility = View.GONE
        }
    }

    private fun updateSpotifyViews(spotifyUser: UserPrivate?, animate: Boolean) {
        if (spotifyUser?.id == null) {
            descriptionTextView.text =
                descriptionTextView.context.getString(R.string.on_boarding_spotify_description_not_logged)

            if (animate) {
                loginSpotifyButton.apply {
                    alpha = 0f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setListener(null)
                }

                loggedContainer.apply {
                    alpha = 1f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                loggedContainer.visibility = View.INVISIBLE
                            }
                        })
                }
            } else {
                loginSpotifyButton.apply {
                    alpha = 1f
                    visibility = View.VISIBLE
                }

                loggedContainer.apply {
                    alpha = 1f
                    visibility = View.GONE
                }
            }
        } else {
            if (animate) {
                loginSpotifyButton.apply {
                    alpha = 1f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                loginSpotifyButton.visibility = View.INVISIBLE
                            }
                        })
                }

                loggedContainer.apply {
                    alpha = 0f
                    visibility = View.VISIBLE

                    animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setListener(null)
                }
            } else {
                loginSpotifyButton.apply {
                    alpha = 1f
                    visibility = View.GONE
                }

                loggedContainer.apply {
                    alpha = 1f
                    visibility = View.VISIBLE
                }
            }

            descriptionTextView.text =
                descriptionTextView.context.getString(R.string.on_boarding_spotify_description_logged)

            pseudoTextView.text = spotifyUser.display_name

            val imageUrl = if (spotifyUser.images != null && spotifyUser.images.size > 0) {
                spotifyUser.images[0].url
            } else {
                null
            }

            profileImageView.clipToOutline = true
            ImageLoader.get()
                .placeholder(R.drawable.placeholder_circle)
                .load(imageUrl)
                .into(profileImageView)
        }

        loginSpotifyButton.setOnClickListener {
            if (activity is BaseSpotifyActivity && context != null) {
                (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.authorize(
                    activity as BaseSpotifyActivity,
                    BaseSpotifyActivity.REQUEST_CODE_SPOTIFY_LOGIN
                )
            }
        }

        logoutSpotifyButton.setOnClickListener {
            if (activity is BaseSpotifyActivity) {
                (activity as BaseSpotifyActivity).spotifyAuthClient.logOut()
                updateSpotifyViews(
                    (activity as BaseSpotifyActivity?)?.spotifyAuthClient?.getCurrentUser(),
                    true
                )
            }
        }
    }
}