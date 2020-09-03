package com.caiolandau.devigetredditclient.redditpostlist.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.clear
import coil.load
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost

class PostRecyclerViewAdapter :
    PagedListAdapter<RedditPost, PostRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {
    var onItemClickListener: ((Int) -> Unit) = {}
    var onDismissListener: ((RedditPost) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context

        val post = getItem(position) ?: return
        holder.txtPosterName.text = post.author
        holder.txtPostTime.text = post.entryDate
        holder.txtPostTitle.text = post.title
        holder.txtPostCommentCount.text =
            context.getString(R.string.num_of_comments, post.numOfComments)
        holder.imgPostThumbnail.load(post.thumbnailUrl) {
            placeholder(R.drawable.ic_reddit_logo)
            error(R.drawable.ic_reddit_logo)
        }

        holder.itemView.setOnClickListener {
            onItemClickListener(position)
        }
        holder.btnDismissPost.setOnClickListener {
            onDismissListener(post)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.imgPostThumbnail.clear()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPosterName: TextView = view.findViewById(R.id.txtPosterName)
        val txtPostTime: TextView = view.findViewById(R.id.txtPostTime)
        val txtPostTitle: TextView = view.findViewById(R.id.txtPostTitle)
        val txtPostCommentCount: TextView = view.findViewById(R.id.txtPostCommentCount)
        val imgPostThumbnail: ImageView = view.findViewById(R.id.imgPostThumbnail)
        val btnDismissPost: Button = view.findViewById(R.id.btnDismissPost)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RedditPost>() {
            override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
                Log.d("CFL", "areItemsTheSame")
                return oldItem.name == newItem.name
            }


            override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
                Log.d("CFL", "areContentsTheSame")
                return oldItem == newItem
            }
        }
    }
}