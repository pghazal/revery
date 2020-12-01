package com.pghaz.revery.adapter.spotify

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.widget.PopupMenu
import com.pghaz.revery.R
import com.pghaz.revery.adapter.base.BaseViewHolder
import com.pghaz.revery.model.app.BaseModel
import com.pghaz.revery.model.app.MediaMetadata
import com.pghaz.spotify.webapi.auth.SpotifyAuthorizationClient


abstract class BaseSpotifyViewHolder(view: View) : BaseViewHolder(view) {

    var onSpotifyItemClickListener: OnSpotifyItemClickListener? = null

    protected val titleTextView: TextView = view.findViewById(R.id.titleTextView)
    protected val subtitleTextView: TextView = view.findViewById(R.id.subtitleTextView)
    protected val imageView: ImageView = view.findViewById(R.id.imageView)
    protected val staticTextView: TextView = view.findViewById(R.id.staticTextView)
    protected val moreButton: ImageButton = view.findViewById(R.id.moreButton)

    @CallSuper
    override fun bind(model: BaseModel) {
        itemView.setOnClickListener {
            onSpotifyItemClickListener?.onSpotifyItemClicked(model)
        }
    }

    protected fun bindAlarmMetadata(metadata: MediaMetadata) {
        bind(metadata.name, metadata.description, metadata.imageUrl)
    }

    abstract fun bind(title: String?, subtitle: String?, imageUrl: String?)

    @CallSuper
    override fun onViewHolderRecycled() {
        itemView.setOnClickListener(null)
    }

    protected fun showMoreMenu(uriString: String, externalUrl: String) {
        val popup = PopupMenu(itemView.context, moreButton)
        popup.menuInflater.inflate(R.menu.menu_item_spotify, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_go_to_spotify -> {
                    if (SpotifyAuthorizationClient.isSpotifyInstalled(itemView.context)) {
                        openInSpotify(uriString)
                    } else {
                        openInBrowser(externalUrl)
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun openInBrowser(externalUrl: String) {
        val builtUri = Uri.parse(externalUrl)
            .buildUpon()
            .appendQueryParameter("utm_campaign", itemView.context.packageName)
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = builtUri
        itemView.context.startActivity(intent)
    }

    private fun openInSpotify(uriString: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uriString)
        intent.putExtra(
            Intent.EXTRA_REFERRER,
            Uri.parse("android-app://" + itemView.context.packageName)
        )
        itemView.context.startActivity(intent)
    }
}