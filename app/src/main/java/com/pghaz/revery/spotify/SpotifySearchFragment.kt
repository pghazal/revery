package com.pghaz.revery.spotify

import android.os.Bundle
import com.pghaz.revery.R
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.revery.util.Arguments

class SpotifySearchFragment : BaseSpotifyFragment(), SpotifySearchListener {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_spotify_search
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)

        /* searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                scrollListener.reset()
                spotifyItemsViewModel.spotifyItemsLiveData.value = emptyList()
                spotifyItemsViewModel.searchFirstPage(query)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    isSearching = false
                    scrollListener.reset()
                    spotifyItemsViewModel.spotifyItemsLiveData.value = emptyList()
                    spotifyItemsViewModel.fetchFirstPage()
                } else {
                    isSearching = true
                }
                return false
            }
        })*/
    }

    override fun search(query: String?) {
        clear()
        spotifyItemsViewModel.searchFirstPage(query)
    }

    override fun clear() {
        scrollListener.reset()
        spotifyItemsViewModel.spotifyItemsLiveData.value = emptyList()
    }

    companion object {
        const val TAG = "SpotifySearchFragment"

        fun newInstance(accessToken: String, filter: SpotifyFilter): SpotifySearchFragment {
            val fragment = SpotifySearchFragment()

            val args = Bundle()
            args.putString(Arguments.ARGS_ACCESS_TOKEN, accessToken)
            args.putInt(Arguments.ARGS_SPOTIFY_FILTER, filter.ordinal)

            fragment.arguments = args

            return fragment
        }
    }
}