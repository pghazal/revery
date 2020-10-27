package com.pghaz.revery.spotify

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import com.pghaz.revery.R
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationCallback
import kotlinx.android.synthetic.main.activity_spotify.*

class SpotifySearchActivity : BaseSpotifyActivity(), SpotifyAuthorizationCallback.Authorize,
    SpotifyAuthorizationCallback.RefreshToken {

    private lateinit var searchView: SearchView

    private var spotifySearchFragment: SpotifySearchFragment? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_spotify_search
    }

    override fun shouldAnimateOnCreate(): Boolean {
        return true
    }

    override fun shouldAnimateOnFinish(): Boolean {
        return true
    }

    override fun onSpotifyAuthorizedAndAvailable() {
        progressBar.visibility = View.GONE
        showDefaultSpotifyFragment()
    }

    private fun showDefaultSpotifyFragment() {
        val accessToken = spotifyAuthClient.getLastTokenResponse()?.accessToken!!

        spotifySearchFragment =
            supportFragmentManager.findFragmentByTag(SpotifySearchFragment.TAG) as SpotifySearchFragment?
        if (spotifySearchFragment == null) {
            spotifySearchFragment =
                SpotifySearchFragment.newInstance(accessToken, SpotifyFilter.NONE)
        }

        replaceFragment(spotifySearchFragment!!, SpotifySearchFragment.TAG)
    }

    override fun parseArguments(args: Bundle?) {
        // do nothing
    }

    override fun configureViews(savedInstanceState: Bundle?) {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_spotify_search, menu)

        val searchMenuItem = menu.findItem(R.id.searchMenuItem)
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return false
            }
        })
        searchMenuItem.expandActionView()

        searchView = searchMenuItem.actionView as SearchView
        searchView.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        searchView.queryHint = getString(R.string.search_in_spotify)
        searchView.requestFocus()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    spotifySearchFragment?.clear()
                } else {
                    spotifySearchFragment?.search(newText)
                }
                return false
            }
        })

        return true
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