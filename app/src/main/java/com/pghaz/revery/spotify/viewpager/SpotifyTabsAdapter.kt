package com.pghaz.revery.spotify.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pghaz.revery.spotify.SpotifyFragment

class SpotifyTabsAdapter(fragmentActivity: FragmentActivity, private val accessToken: String) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = SpotifyTabs.values().size

    override fun createFragment(position: Int): Fragment {
        return SpotifyFragment.newInstance(accessToken, SpotifyTabs.values()[position].filter)
    }
}