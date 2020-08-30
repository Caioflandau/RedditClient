package com.caiolandau.devigetredditclient.home.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.dummy.DummyContent
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.home.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_post_list.*
import java.lang.ref.WeakReference

/**
 * An activity representing a list of RedditPosts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [PostDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class PostListActivityWrapper(
    activity: IViewModelActivity<PostListViewModel>
) {
    // Keeping a weak reference to the activity prevents a reference loop (and memory leak):
    private val weakActivity: WeakReference<IViewModelActivity<PostListViewModel>> = WeakReference(activity)
    private val activity: IViewModelActivity<PostListViewModel>?
        get() = weakActivity.get()

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    fun onCreate(savedInstanceState: Bundle?) = activity?.apply {
        findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (findViewById<NestedScrollView>(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        bindOutput(getViewModel())
    }

    private fun bindOutput(viewModel: PostListViewModel) = activity?.apply {
        viewModel.output.listOfPosts
            .observe(this) {
                findViewById<RecyclerView>(R.id.item_list)?.apply {
                    setupRecyclerView(this, DummyContent.ITEMS)
                }
            }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        posts: MutableList<DummyContent.DummyItem>
    ) = activity?.apply {
        recyclerView.adapter =
            SimpleItemRecyclerViewAdapter(
                this,
                posts,
                twoPane
            )
    }
}

class PostListActivity : AppCompatActivity(), IViewModelActivity<PostListViewModel> {
    // In order to avoid needing something like Robolectric to test activity logic, we use a wrapper
    // class. That wrapper is just a regular class that can be instantiated easily, and contains all
    // Activity business logic. The actual Activity subclass is just a shell.
    private val activityWrapper = PostListActivityWrapper(this)
    override val context
        get() = this

    override fun getViewModel(): PostListViewModel {
        val viewModel: PostListViewModel by viewModels()
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)
        toolbar.title = title

        activityWrapper.onCreate(savedInstanceState)
    }
}