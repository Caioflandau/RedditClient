package com.caiolandau.devigetredditclient.home.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.dummy.DummyContent
import com.caiolandau.devigetredditclient.home.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity

class SimpleItemRecyclerViewAdapter(
    private val parentActivity: IViewModelActivity<PostListViewModel>,
    private val values: List<DummyContent.DummyItem>,
    private val twoPane: Boolean
) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as DummyContent.DummyItem
            if (twoPane) {
                val fragment = PostDetailFragment()
                    .apply {
                        arguments = Bundle().apply {
                            putString(PostDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                parentActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(v.context, PostDetailActivity::class.java).apply {
                    putExtra(PostDetailFragment.ARG_ITEM_ID, item.id)
                }
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = values.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.id_text)
        val contentView: TextView = view.findViewById(R.id.content)
    }
}