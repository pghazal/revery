package com.pghaz.revery.spotify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.chip.Chip
import com.pghaz.revery.R
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.revery.view.ExtendedFloatingActionListener
import io.github.kaaes.spotify.webapi.core.models.UserPrivate
import kotlinx.android.synthetic.main.activity_spotify.*
import net.openid.appauth.TokenResponse

class SpotifyActivity : BaseSpotifyActivity(), ExtendedFloatingActionListener {

    companion object {
        const val REQUEST_CODE_SPOTIFY_SEARCH = 1338
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_spotify
    }

    override fun shouldAnimateOnCreate(): Boolean {
        return true
    }

    override fun shouldAnimateOnFinish(): Boolean {
        return true
    }

    override fun shouldShowAuth(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSpotifyAuthorizedAndAvailable() {
        progressBar.visibility = View.GONE
        showDefaultSpotifyFragment()
    }

    private fun showDefaultSpotifyFragment() {
        showSearchButton()
        showFilter()
        showSpotifyFragment(SpotifyFilter.RECENTLY_PLAYED)
    }

    private fun showSearchButton() {
        searchButton.visibility = View.VISIBLE
    }

    private fun showFilter() {
        filtersContainer.visibility = View.VISIBLE
    }

    override fun parseArguments(args: Bundle?) {
        // do nothing
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        configureFilters()

        searchButton.setOnClickListener {
            val intent = Intent(this, SpotifySearchActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SPOTIFY_SEARCH)
        }
    }

    private fun scrollToChip(chip: Chip) {
        filterChipGroup.post {
            val screenWidth = resources.displayMetrics.widthPixels
            val point = IntArray(2)
            chip.getLocationOnScreen(point)
            val x = point[0]

            if (x + chip.width + chip.paddingRight > screenWidth) {
                filtersContainer.smoothScrollBy(
                    (x + chip.width + chip.paddingRight) - screenWidth,
                    0
                )
            } else if (x < 0) {
                filtersContainer.smoothScrollBy(x - chip.paddingRight, 0)
            }
        }
    }

    private fun configureFilters() {
        filterMyPlaylistsChip.setOnClickListener {
            scrollToChip(filterMyPlaylistsChip)
            showSpotifyFragment(SpotifyFilter.MY_PLAYLISTS)
        }

        filterMyTopArtistsChip.setOnClickListener {
            scrollToChip(filterMyTopArtistsChip)
            showSpotifyFragment(SpotifyFilter.MY_TOP_ARTISTS)
        }

        filterMyTopTracksChip.setOnClickListener {
            scrollToChip(filterMyTopTracksChip)
            showSpotifyFragment(SpotifyFilter.MY_TOP_TRACKS)
        }

        filterRecentlyPlayerChip.setOnClickListener {
            scrollToChip(filterRecentlyPlayerChip)
            showSpotifyFragment(SpotifyFilter.RECENTLY_PLAYED)
        }
    }

    private fun showSpotifyFragment(filter: SpotifyFilter) {
        val accessToken = spotifyAuthClient.getLastTokenResponse()?.accessToken!!

        replaceFragment(
            SpotifyFragment.newInstance(accessToken, filter),
            SpotifyFragment.TAG
        )
    }

    override fun extendFloatingActionButton() {
        searchButton.extend()
    }

    override fun shrinkFloatingActionButton() {
        searchButton.shrink()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPOTIFY_SEARCH) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    override fun onAuthorizationStarted() {
        super.onAuthorizationStarted()
        progressBar.visibility = View.VISIBLE
    }

    override fun onRefreshAccessTokenStarted() {
        super.onRefreshAccessTokenStarted()
        progressBar.visibility = View.VISIBLE
    }
}