package com.caiolandau.devigetredditclient.home.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.dummy.DummyContent
import com.caiolandau.devigetredditclient.home.model.RedditPost

/**
 * A fragment representing a single post detail screen.
 * This fragment is either contained in a [PostListActivity] in two-pane mode (on tablets)
 * or a [PostDetailActivity] on handsets.
 */
class PostDetailFragment : Fragment() {

    /**
     * The post this fragment is presenting.
     */
    private var post: RedditPost? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_POST)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                post = it.getParcelable(ARG_POST)
                activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = post?.title
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.post_detail, container, false)

        post?.let {
            rootView.findViewById<TextView>(R.id.item_detail).text = it.title
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the post that this fragment represents
         */
        const val ARG_POST = "post"
    }
}