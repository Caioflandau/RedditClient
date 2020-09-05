package com.caiolandau.devigetredditclient.redditpostdetail.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostlist.view.PostListActivity
import com.caiolandau.devigetredditclient.util.IActivity
import java.lang.ref.WeakReference

/**
 * An activity representing a single RedditPost detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [PostListActivity].
 */
class PostDetailActivityWrapper(
    activity: IActivity,
    private val makePostDetailFragment: (Intent) -> PostDetailFragment = { intent ->
        PostDetailFragment()
            .apply {
                arguments = Bundle().apply {
                    putParcelable(
                        PostDetailFragment.ARG_POST,
                        intent.getParcelableExtra(PostDetailFragment.ARG_POST)
                    )
                }
            }
    }
) {
    // Keeping a weak reference to the activity prevents a reference loop (and memory leak):
    private val weakActivity: WeakReference<IActivity> = WeakReference(activity)
    private val activity: IActivity?
        get() = weakActivity.get()

    fun onCreate(savedInstanceState: Bundle?) = activity?.apply {
        // Show the Up button in the action bar.
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = makePostDetailFragment(getIntent())

            getSupportFragmentManager().beginTransaction()
                .add(R.id.frmPostDetailContainer, fragment)
                .commit()
        }
    }

    fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {

            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back

            activity?.apply {
                navigateUpTo(Intent(context, PostListActivity::class.java))
            }
            true
        }
        else -> true
    }
}

class PostDetailActivity : AppCompatActivity(), IActivity {
    // In order to avoid needing something like Robolectric to test activity logic, we use a wrapper
    // class. That wrapper is just a regular class that can be instantiated easily, and contains all
    // Activity business logic. The actual Activity subclass is just a shell.
    private val activityWrapper = PostDetailActivityWrapper(this)
    override val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        setSupportActionBar(findViewById(R.id.toolbarDetail))

        activityWrapper.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem) = activityWrapper.onOptionsItemSelected(item)

    companion object {
        fun newIntent(context: Context, post: RedditPost): Intent =
            Intent(context, PostDetailActivity::class.java).apply {
                putExtra(PostDetailFragment.ARG_POST, post)
            }
    }
}