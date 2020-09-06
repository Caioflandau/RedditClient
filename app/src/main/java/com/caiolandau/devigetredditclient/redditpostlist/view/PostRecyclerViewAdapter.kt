package com.caiolandau.devigetredditclient.redditpostlist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.clear
import coil.load
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost

class PostRecyclerViewAdapter(
    private val imageLoader: ImageLoader
) :
    PagedListAdapter<RedditPost, PostRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK) {
    var onItemClickListener: ((RedditPost) -> Unit) = {}
    var onDismissListener: ((RedditPost) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bindViewHolder(getItem(position) ?: return, holder)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.imgPostThumbnail.clear() // Clears any pending image load request
    }

    fun bindViewHolder(post: RedditPost, viewHolder: ViewHolder) = viewHolder.apply {
        val context = itemView.context

        txtPosterName.text = post.author
        txtPostTime.text = post.entryDate
        txtPostTitle.text = post.title
        txtPostCommentCount.text =
            context.getString(R.string.num_of_comments, post.numOfComments)
        imgPostThumbnail.load(post.thumbnailUrl, imageLoader) {
            placeholder(R.drawable.ic_reddit_logo)
            error(R.drawable.ic_reddit_logo)
        }
        frmUnreadIndicator.visibility = if (post.isRead) View.GONE else View.VISIBLE

        itemView.setOnClickListener {
            onItemClickListener(post)
            frmUnreadIndicator.visibility = View.GONE
        }
        btnDismissPost.setOnClickListener {
            onDismissListener(post)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPosterName: TextView = view.findViewById(R.id.txtPosterName)
        val txtPostTime: TextView = view.findViewById(R.id.txtPostTime)
        val txtPostTitle: TextView = view.findViewById(R.id.txtPostTitle)
        val txtPostCommentCount: TextView = view.findViewById(R.id.txtPostCommentCount)
        val imgPostThumbnail: ImageView = view.findViewById(R.id.imgPostThumbnail)
        val btnDismissPost: Button = view.findViewById(R.id.btnDismissPost)
        val frmUnreadIndicator: FrameLayout = view.findViewById(R.id.frmUnreadIndicator)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RedditPost>() {
            override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
                return oldItem == newItem
            }
        }
    }
}