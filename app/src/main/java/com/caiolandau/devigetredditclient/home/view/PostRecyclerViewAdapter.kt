package com.caiolandau.devigetredditclient.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.home.model.RedditPost

class PostRecyclerViewAdapter : RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder>() {
    private var posts: List<RedditPost> = emptyList()

    var onItemClickListener: ((Int) -> Unit) = {}

    fun setPosts(newPosts: List<RedditPost>) {
        posts = newPosts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.txtPosterName.text = post.author
        holder.txtPostTime.text = post.entryDate
        holder.txtPostTitle.text = post.title

        holder.itemView.setOnClickListener {
            onItemClickListener(position)
        }
    }

    override fun getItemCount() = posts.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPosterName: TextView = view.findViewById(R.id.txtPosterName)
        val txtPostTime: TextView = view.findViewById(R.id.txtPostTime)
        val txtPostTitle: TextView = view.findViewById(R.id.txtPostTitle)
        val imgPostThumbnail: ImageView = view.findViewById(R.id.imgPostThumbnail)
    }
}