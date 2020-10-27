package com.pghaz.revery.spotify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.adapter.spotify.OnSpotifyItemClickListener
import com.pghaz.revery.adapter.spotify.SpotifyItemsAdapter
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.spotify.SpotifyFilter
import com.pghaz.revery.util.Arguments
import com.pghaz.revery.view.ResultListScrollListener
import com.pghaz.revery.viewmodel.spotify.SpotifyErrorListener
import com.pghaz.revery.viewmodel.spotify.SpotifyItemsViewModel
import com.pghaz.revery.viewmodel.spotify.SpotifyViewModelFactory
import io.github.kaaes.spotify.webapi.retrofit.v2.SpotifyError
import kotlinx.android.synthetic.main.fragment_spotify.*

abstract class BaseSpotifyFragment : BaseFragment(), ResultListScrollListener.OnLoadMoreListener,
    OnSpotifyItemClickListener, SpotifyErrorListener {

    private lateinit var accessToken: String
    private lateinit var filter: SpotifyFilter

    protected lateinit var spotifyItemsViewModel: SpotifyItemsViewModel

    protected lateinit var scrollListener: ResultListScrollListener
    private lateinit var itemsAdapter: SpotifyItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemsAdapter = SpotifyItemsAdapter(this)

        spotifyItemsViewModel =
            ViewModelProvider(this, SpotifyViewModelFactory(accessToken, filter))
                .get(SpotifyItemsViewModel::class.java)
        spotifyItemsViewModel.spotifyErrorListener = this
        spotifyItemsViewModel.spotifyItemsLiveData.observe(this, {
            itemsAdapter.submitList(it)
        })
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

    override fun configureViews(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(context)
        scrollListener =
            ResultListScrollListener(layoutManager, this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = itemsAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onClick(model: BaseModel) {
        val data = Intent()
        data.putExtra(Arguments.ARGS_SPOTIFY_ITEM_SELECTED, model)
        activity?.setResult(Activity.RESULT_OK, data)
        activity?.finish()
    }

    override fun onSpotifyError(error: SpotifyError) {
        val errorMessage = when (error.details.status) {
            SpotifyError.ERROR_NETWORK -> getString(R.string.error_network)
            else -> getString(R.string.error_unexpected)
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}