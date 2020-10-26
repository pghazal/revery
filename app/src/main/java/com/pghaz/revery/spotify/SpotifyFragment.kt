package com.pghaz.revery.spotify

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.adapter.spotify.OnSpotifyItemClickListener
import com.pghaz.revery.adapter.spotify.SpotifyItemsAdapter
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.view.ExtendedFloatingActionListener
import com.pghaz.revery.view.ResultListScrollListener
import com.pghaz.revery.viewmodel.spotify.SpotifyItemsViewModel
import com.pghaz.revery.viewmodel.spotify.SpotifyViewModelFactory
import kotlinx.android.synthetic.main.fragment_spotify.*

class SpotifyFragment : BaseFragment(), ResultListScrollListener.OnLoadMoreListener,
    OnSpotifyItemClickListener {

    private lateinit var accessToken: String
    private lateinit var filter: SpotifyFilter

    private lateinit var spotifyItemsViewModel: SpotifyItemsViewModel

    private lateinit var floatingActionListener: ExtendedFloatingActionListener
    private lateinit var scrollListener: ResultListScrollListener
    private lateinit var itemsAdapter: SpotifyItemsAdapter

    private var isSearching = false

    override fun getLayoutResId(): Int {
        return R.layout.fragment_spotify
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemsAdapter = SpotifyItemsAdapter(this)

        spotifyItemsViewModel =
            ViewModelProvider(this, SpotifyViewModelFactory(accessToken, filter))
                .get(SpotifyItemsViewModel::class.java)
        spotifyItemsViewModel.spotifyItemsLiveData.observe(this, {
            itemsAdapter.submitList(it)
        })
        spotifyItemsViewModel.fetchFirstPage()
    }

    override fun parseArguments(arguments: Bundle?) {
        super.parseArguments(arguments)
        accessToken = arguments?.getString(Arguments.ARGS_ACCESS_TOKEN)!!
        filter = SpotifyFilter.values()[arguments.getInt(Arguments.ARGS_SPOTIFY_FILTER)]
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(Arguments.ARGS_ACCESS_TOKEN, accessToken)
        outState.putInt(Arguments.ARGS_SPOTIFY_FILTER, filter.ordinal)
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        floatingActionListener = context as ExtendedFloatingActionListener
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(context)
        scrollListener =
            ResultListScrollListener(layoutManager, this, floatingActionListener)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = itemsAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addOnScrollListener(scrollListener)

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
        })
    }

    override fun onLoadMore() {
        if (isSearching) {
            spotifyItemsViewModel.searchNextPage()
        } else {
            spotifyItemsViewModel.fetchNextPage()
        }
    }

    override fun onClick(model: BaseModel) {
        val data = Intent()
        data.putExtra(Arguments.ARGS_SPOTIFY_ITEM_SELECTED, model)
        activity?.setResult(Activity.RESULT_OK, data)
        activity?.finish()
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