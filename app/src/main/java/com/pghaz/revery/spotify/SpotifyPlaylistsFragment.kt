package com.pghaz.revery.spotify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.spotify.adapter.OnSpotifyItemClickListener
import com.pghaz.revery.spotify.adapter.SpotifyItemsAdapter
import com.pghaz.revery.spotify.model.PlaylistWrapper
import com.pghaz.revery.spotify.viewmodel.SpotifyItemsViewModel
import com.pghaz.revery.spotify.viewmodel.SpotifyViewModelFactory
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.view.ResultListScrollListener
import kotlinx.android.synthetic.main.fragment_spotify_playlists.*

class SpotifyPlaylistsFragment : BaseFragment(), ResultListScrollListener.OnLoadMoreListener,
    OnSpotifyItemClickListener {

    private lateinit var accessToken: String
    private lateinit var spotifyItemsViewModel: SpotifyItemsViewModel

    private lateinit var scrollListener: ResultListScrollListener
    private lateinit var itemsAdapter: SpotifyItemsAdapter

    override fun getLayoutResId(): Int {
        return R.layout.fragment_spotify_playlists
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemsAdapter = SpotifyItemsAdapter(this)

        spotifyItemsViewModel = ViewModelProvider(this, SpotifyViewModelFactory(accessToken))
            .get(SpotifyItemsViewModel::class.java)
        spotifyItemsViewModel.spotifyItemsLiveData.observe(this, {
            itemsAdapter.submitList(it)
        })
        spotifyItemsViewModel.getFirstPage()
    }

    override fun parseArguments(arguments: Bundle?) {
        super.parseArguments(arguments)
        accessToken = arguments?.getString(Arguments.ARGS_ACCESS_TOKEN)!!
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(Arguments.ARGS_ACCESS_TOKEN, accessToken)
        super.onSaveInstanceState(outState)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(context)
        scrollListener = ResultListScrollListener(layoutManager, this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = itemsAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onLoadMore() {
        spotifyItemsViewModel.getNextPage()
    }

    override fun onClick(playlistWrapper: PlaylistWrapper) {
        val data = Intent()
        data.putExtra(Arguments.ARGS_SPOTIFY_ITEM_SELECTED, playlistWrapper)
        activity?.setResult(Activity.RESULT_OK, data)
        activity?.finish()
    }

    companion object {
        const val TAG = "SpotifyPlaylistsFragment"

        fun newInstance(accessToken: String): SpotifyPlaylistsFragment {
            val fragment = SpotifyPlaylistsFragment()

            val args = Bundle()
            args.putString(Arguments.ARGS_ACCESS_TOKEN, accessToken)

            fragment.arguments = args

            return fragment
        }
    }
}