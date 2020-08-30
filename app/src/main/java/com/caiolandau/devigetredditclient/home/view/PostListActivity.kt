package com.caiolandau.devigetredditclient.home.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.home.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_post_list.*
import java.lang.ref.WeakReference


/**
 * An activity representing a list of RedditPosts. This activity has different presentations for
 * handset and tablet-size devices.
 * On handsets, the activity presents a list of items, which when touched, lead to a
 * [PostDetailActivity] representing item details.
 * On tablets, the activity presents the list of items and item details side-by-side
 * using two vertical panes.
 */
class PostListActivityWrapper(
    activity: IViewModelActivity<PostListViewModel>
) {
    // Keeping a weak reference to the activity prevents a reference loop (and memory leak):
    private val weakActivity: WeakReference<IViewModelActivity<PostListViewModel>> = WeakReference(
        activity
    )
    private val activity: IViewModelActivity<PostListViewModel>?
        get() = weakActivity.get()

    private var recyclerViewAdapter: PostRecyclerViewAdapter? = null

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private var twoPane: Boolean = false

    fun onCreate(savedInstanceState: Bundle?) = activity?.apply {
        setupRecyclerView()

        findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (findViewById<NestedScrollView>(R.id.frmPostDetailContainer) != null) {
            // The detail container will be present only in the large-screen layouts (res/values-w900dp).
            // If this view is present, then the activity should be in two-pane mode.
            twoPane = true
        }

        val viewModel = getViewModel()
        bindOutput(viewModel)
        bindInput(viewModel)
    }

    private fun bindInput(viewModel: PostListViewModel) = activity?.apply {
        val adapter = findViewById<RecyclerView>(R.id.recyclerViewPosts)?.adapter
        (adapter as? PostRecyclerViewAdapter)?.let { adapter ->
            adapter.onItemClickListener = viewModel.input.onClickPostListItem::onNext
        }
    }

    private fun bindOutput(viewModel: PostListViewModel) = activity?.apply {
        viewModel.output.listOfPosts
            .observe(this) { posts ->
                recyclerViewAdapter?.setPosts(posts)
            }

        viewModel.output.showPostDetails
            .observe(this) { post ->
                val post = post.getContentIfNotHandled() ?: return@observe
                if (twoPane) {
                    val fragment = PostDetailFragment()
                        .apply {
                            arguments = Bundle().apply {
                                putParcelable(PostDetailFragment.ARG_POST, post)
                            }
                        }
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frmPostDetailContainer, fragment)
                        .commit()
                } else {
                    val intent = Intent(context, PostDetailActivity::class.java).apply {
                        putExtra(PostDetailFragment.ARG_POST, post)
                    }
                    context.startActivity(intent)
                }
            }
    }

    private fun setupRecyclerView() = activity?.apply {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPosts)
        recyclerView?.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        recyclerViewAdapter = PostRecyclerViewAdapter()
        recyclerView?.adapter = recyclerViewAdapter
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
        // ViewModels are created once per scope (i.e. Activity) and reused for as long as the scope
        // is alive. It's fine to use `by viewModels()` every time instead of holding an instance of
        // the ViewModel:
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