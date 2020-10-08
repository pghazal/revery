package com.pghaz.revery.spotify

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pghaz.revery.BaseFragment
import com.pghaz.revery.R
import com.pghaz.revery.adapter.AlarmsAdapter
import kotlinx.android.synthetic.main.fragment_spotify_playlists.*

class SpotifyPlaylistsFragment : BaseFragment() {

    private lateinit var itemsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    override fun getLayoutResId(): Int {
        return R.layout.fragment_spotify_playlists
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //itemsAdapter = AlarmsAdapter(this)
    }

    override fun configureViews(savedInstanceState: Bundle?) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        //recyclerView.adapter = itemsAdapter
    }

    companion object {
        const val TAG = "SpotifyPlaylistsFragment"

        fun newInstance(): SpotifyPlaylistsFragment {
            val fragment = SpotifyPlaylistsFragment()

            val args = Bundle()
            fragment.arguments = args

            return fragment
        }
    }
}