package com.caiolandau.devigetredditclient.home.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.dummy.DummyContent
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.home.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity

class PostRecyclerViewAdapter(
    private val values: List<RedditPost>
) : RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder>() {

//    init {
//        onClickListener = View.OnClickListener { v ->
//            val item = v.tag as RedditPost
//            if (twoPane) {
//                val fragment = PostDetailFragment()
//                    .apply {
//                        arguments = Bundle().apply {
//                            putString(PostDetailFragment.ARG_ITEM_ID, item.id)
//                        }
//                    }
//                parentActivity.getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.item_detail_container, fragment)
//                    .commit()
//            } else {
//                val intent = Intent(v.context, PostDetailActivity::class.java).apply {
//                    putExtra(PostDetailFragment.ARG_ITEM_ID, item.id)
//                }
//                v.context.startActivity(intent)
//            }
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = values[position]
        holder.txtPosterName.text = post.author
        holder.txtPostTime.text = post.entryDate
        holder.txtPostTitle.text = post.title
    }

    override fun getItemCount() = values.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPosterName: TextView = view.findViewById(R.id.txtPosterName)
        val txtPostTime: TextView = view.findViewById(R.id.txtPostTime)
        val txtPostTitle: TextView = view.findViewById(R.id.txtPostTitle)
        val imgPostThumbnail: ImageView = view.findViewById(R.id.imgPostThumbnail)
    }
}