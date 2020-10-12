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
import com.pghaz.revery.spotify.viewmodel.SpotifyItemsViewModel
import com.pghaz.revery.spotify.viewmodel.SpotifyViewModelFactory
import com.pghaz.revery.view.ResultListScrollListener
import kaaes.spotify.webapi.android.models.PlaylistSimple
import kotlinx.android.synthetic.main.fragment_spotify_playlists.*

class SpotifyPlaylistsFragment : BaseFragment(), ResultListScrollListener.OnLoadMoreListener {

    private lateinit var spotifyItemsViewModel: SpotifyItemsViewModel
    private lateinit var scrollListener: ResultListScrollListener
    private lateinit var itemsAdapter: SpotifyItemsAdapter

    override fun getLayoutResId(): Int {
        return R.layout.fragment_spotify_playlists
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessToken = arguments?.getString(ARGS_ACCESS_TOKEN)

        itemsAdapter = SpotifyItemsAdapter(object : OnSpotifyItemClickListener {
            override fun onClick(playlist: PlaylistSimple) {
                val data = Intent()
                data.putExtra(ARGS_SPOTIFY_SELECTED_PLAYLIST, playlist)
                activity?.setResult(Activity.RESULT_OK, data)
                activity?.finish()
            }
        })

        spotifyItemsViewModel = ViewModelProvider(this, SpotifyViewModelFactory(accessToken))
            .get(SpotifyItemsViewModel::class.java)
        spotifyItemsViewModel.spotifyItemsLiveData.observe(this, {
            itemsAdapter.addItems(it)
        })
        spotifyItemsViewModel.getFirstPage()
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

    companion object {
        const val TAG = "SpotifyPlaylistsFragment"

        private const val ARGS_ACCESS_TOKEN = "ARGS_ACCESS_TOKEN"
        const val ARGS_SPOTIFY_SELECTED_PLAYLIST = "ARGS_SPOTIFY_SELECTED_PLAYLIST"

        fun newInstance(accessToken: String): SpotifyPlaylistsFragment {
            val fragment = SpotifyPlaylistsFragment()

            val args = Bundle()
            args.putString(ARGS_ACCESS_TOKEN, accessToken)

            fragment.arguments = args

            return fragment
        }
    }
}