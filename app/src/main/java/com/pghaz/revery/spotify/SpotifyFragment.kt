package com.pghaz.revery.spotify

import android.content.Context
import android.os.Bundle
import com.pghaz.revery.R
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.view.ExtendedFloatingActionListener

class SpotifyFragment : BaseSpotifyFragment() {

    private var floatingActionListener: ExtendedFloatingActionListener? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_spotify
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        spotifyItemsViewModel.fetchFirstPage()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        floatingActionListener = context as? ExtendedFloatingActionListener
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        super.configureViews(savedInstanceState)
        scrollListener.floatingActionListener = floatingActionListener

        /*searchView.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    companion object {
        const val TAG = "SpotifyFragment"

        fun newInstance(accessToken: String, filter: SpotifyFilter): SpotifyFragment {
            val fragment = SpotifyFragment()

            val args = Bundle()
            args.putString(Arguments.ARGS_ACCESS_TOKEN, accessToken)
            args.putInt(Arguments.ARGS_SPOTIFY_FILTER, filter.ordinal)

            fragment.arguments = args

            return fragment
        }
    }
}