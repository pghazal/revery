package com.pghaz.revery.spotify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.pghaz.revery.R
import com.pghaz.revery.spotify.viewpager.SpotifyTabs
import com.pghaz.revery.spotify.viewpager.SpotifyTabsAdapter
import com.pghaz.revery.view.ExtendedFloatingActionListener
import kotlinx.android.synthetic.main.activity_spotify.*

class SpotifyActivity : BaseSpotifyActivity(), ExtendedFloatingActionListener {

    companion object {
        const val REQUEST_CODE_SPOTIFY_SEARCH = 1338
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_spotify
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
        showSearchButton()
        showTabs()
        fillTabLayout()
    }

    private fun showSearchButton() {
        searchButton.visibility = View.VISIBLE
    }

    private fun showTabs() {
        tabLayout.visibility = View.VISIBLE
    }

    override fun parseArguments(args: Bundle?) {
        // do nothing
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)
        searchButton.setOnClickListener {
            val intent = Intent(this, SpotifySearchActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SPOTIFY_SEARCH)
        }
    }

    private fun fillTabLayout() {
        val accessToken = spotifyAuthClient.getLastTokenResponse()?.accessToken!!
        viewPager.adapter = SpotifyTabsAdapter(this, accessToken)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(SpotifyTabs.values()[position].textResId)
        }.attach()
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