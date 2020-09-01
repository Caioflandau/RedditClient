package com.caiolandau.devigetredditclient.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.home.model.RedditPost

class PostRecyclerViewAdapter : PagedListAdapter<RedditPost, PostRecyclerViewAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<RedditPost>() {
        override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost) =
            oldItem == newItem

    }
) {
    var onItemClickListener: ((Int) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.txtPosterName.text = post.author
        holder.txtPostTime.text = post.entryDate
        holder.txtPostTitle.text = post.title

        holder.itemView.setOnClickListener {
            onItemClickListener(position)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPosterName: TextView = view.findViewById(R.id.txtPosterName)
        val txtPostTime: TextView = view.findViewById(R.id.txtPostTime)
        val txtPostTitle: TextView = view.findViewById(R.id.txtPostTitle)
        val imgPostThumbnail: ImageView = view.findViewById(R.id.imgPostThumbnail)
    }
}