package com.pghaz.revery.spotify

import android.os.Bundle
import com.pghaz.revery.R
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.revery.util.Arguments

class SpotifySearchFragment : BaseSpotifyFragment(), SpotifySearchListener {

    override fun getLayoutResId(): Int {
        return R.layout.fragment_spotify_search
    }

    override fun search(query: String?) {
        clear()
        spotifyItemsViewModel.searchFirstPage(query)
    }

    override fun clear() {
        scrollListener.reset()
        spotifyItemsViewModel.cancelSearch()
        spotifyItemsViewModel.spotifyItemsLiveData.value = emptyList()
    }

    override fun onLoadMore() {
        spotifyItemsViewModel.searchNextPage()
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