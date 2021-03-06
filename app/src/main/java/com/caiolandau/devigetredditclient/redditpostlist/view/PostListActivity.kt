package com.caiolandau.devigetredditclient.redditpostlist.view

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.ImageLoader
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostdetail.view.PostDetailActivity
import com.caiolandau.devigetredditclient.redditpostdetail.view.PostDetailFragment
import com.caiolandau.devigetredditclient.redditpostlist.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity
import com.caiolandau.devigetredditclient.util.SnackbarHelper
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.android.synthetic.main.activity_post_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.sendBlocking
import java.lang.ref.WeakReference

/**
 * An activity representing a list of RedditPosts. This activity has different presentations for
 * handset and tablet-size devices.
 * On handsets, the activity presents a list of items, which when touched, lead to a
 * [PostDetailActivity] representing item details.
 * On tablets, the activity presents the list of items and item details side-by-side
 * using two vertical panes.
 */
@FlowPreview
@ExperimentalCoroutinesApi // Coroutines / Flow are still marked as experimental, although they are considered stable enough
class PostListActivityWrapper(
    activity: IViewModelActivity<PostListViewModel>,
    private val snackbarHelper: SnackbarHelper = SnackbarHelper(),
    private val makePostDetailFragment: (RedditPost) -> PostDetailFragment = { post ->
        PostDetailFragment()
            .apply {
                arguments = Bundle().apply {
                    putParcelable(PostDetailFragment.ARG_POST, post)
                }
            }
    }
) {
    // Keeping a weak reference to the activity prevents a reference loop (and memory leak):
    private val weakActivity: WeakReference<IViewModelActivity<PostListViewModel>> = WeakReference(
        activity
    )
    private val activity: IViewModelActivity<PostListViewModel>?
        get() = weakActivity.get()

    private lateinit var pagedListCallback: PagedList.Callback

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private var twoPane: Boolean = false

    fun onCreate(savedInstanceState: Bundle?) = activity?.apply {
        setupRecyclerView()

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
        val adapter =
            findViewById<RecyclerView>(R.id.recyclerViewPosts)?.adapter as? PostRecyclerViewAdapter
        adapter?.apply {
            onItemClickListener = {
                viewModel.input.onClickPostListItem.sendBlocking(it)
            }
            onDismissListener = {
                viewModel.input.onClickDismissPost.sendBlocking(it)
            }
        }

        findViewById<SwipeRefreshLayout>(R.id.swiperefresh)?.setOnRefreshListener {
            viewModel.input.onRefresh.sendBlocking(Unit)
        }

        findViewById<ExtendedFloatingActionButton>(R.id.btnClearAll)?.setOnClickListener {
            viewModel.input.onClickDismissAll.sendBlocking(Unit)
        }
    }

    private fun bindOutput(viewModel: PostListViewModel) = activity?.apply {
        val adapter =
            findViewById<RecyclerView>(R.id.recyclerViewPosts)?.adapter as? PostRecyclerViewAdapter
        viewModel.output.listOfPosts
            .observe(lifecycleOwner()) { posts ->
                pagedListCallback = object : PagedList.Callback() {
                    override fun onChanged(position: Int, count: Int) {
                        adapter?.submitList(posts)
                    }

                    override fun onInserted(position: Int, count: Int) {
                        adapter?.submitList(posts)
                    }

                    override fun onRemoved(position: Int, count: Int) {
                        adapter?.submitList(posts)
                    }
                }
                if (posts.size > 0) {
                    adapter?.submitList(posts)
                } else {
                    posts.addWeakCallback(null, pagedListCallback)
                }
            }

        viewModel.output.showPostDetails
            .observe(lifecycleOwner()) { postEvent ->
                val post = postEvent.getContentIfNotHandled()
                if (viewModel.output.listOfPosts.value?.contains(post) == true) {
                    if (twoPane) {
                        showDetailsFragment(postEvent.peekContent())
                    } else {
                        post ?: return@observe
                        showDetailsActivity(post)
                    }
                }
            }

        viewModel.output.closePostDetails
            .observe(lifecycleOwner()) { event ->
                if (!event.hasBeenHandled) {
                    event.getContentIfNotHandled()
                    showDetailsFragment(null)
                }
            }

        viewModel.output.errorLoadingPage
            .observe(lifecycleOwner()) { event ->
                if (!event.hasBeenHandled) {
                    event.getContentIfNotHandled()
                    val containerView =
                        findViewById<FrameLayout>(R.id.frmListContainer) ?: return@observe
                    snackbarHelper.showSnackbar(containerView, R.string.error_loading_posts)
                }
            }

        viewModel.output.isRefreshing
            .observe(lifecycleOwner()) {
                findViewById<SwipeRefreshLayout>(R.id.swiperefresh)?.isRefreshing = it
            }

        viewModel.output.clearedAll
            .observe(lifecycleOwner()) { event ->
                if (!event.hasBeenHandled) {
                    event.getContentIfNotHandled()
                    adapter?.submitList(null)
                }
            }
    }

    private fun showDetailsActivity(post: RedditPost?) = activity?.apply {
        post ?: return@apply
        val intent = PostDetailActivity.newIntent(context, post)
        context.startActivity(intent)
    }

    private fun showDetailsFragment(post: RedditPost?) = activity?.apply {
        if (post == null) {
            getSupportFragmentManager().findFragmentById(R.id.frmPostDetailContainer)?.let {
                getSupportFragmentManager()
                    .beginTransaction()
                    .remove(it)
                    .commit()
            }
        } else {
            val fragment = makePostDetailFragment(post)
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frmPostDetailContainer, fragment)
                .commit()
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

        recyclerView?.adapter = recyclerView?.adapter ?:
                PostRecyclerViewAdapter(ImageLoader.Builder(context).build())
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
class PostListActivity : AppCompatActivity(), IViewModelActivity<PostListViewModel> {
    // In order to avoid needing something like Robolectric to test activity logic, we use a wrapper
    // class. That wrapper is just a regular class that can be instantiated easily, and contains all
    // Activity logic. The actual Activity subclass is just a shell.
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