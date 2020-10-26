package com.pghaz.revery.spotify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.pghaz.revery.R
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.revery.view.ExtendedFloatingActionListener
import kotlinx.android.synthetic.main.activity_spotify.*

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

    override fun onSpotifyAuthorizedAndAvailable() {
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

    private fun configureFilters() {
        filterMyPlaylistsChip.setOnClickListener {
            showSpotifyFragment(SpotifyFilter.MY_PLAYLISTS)
        }

        filterMyTopArtistsChip.setOnClickListener {
            showSpotifyFragment(SpotifyFilter.MY_TOP_ARTISTS)
        }

        filterMyTopTracksChip.setOnClickListener {
            showSpotifyFragment(SpotifyFilter.MY_TOP_TRACKS)
        }

        filterRecentlyPlayerChip.setOnClickListener {
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
}