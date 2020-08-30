package com.caiolandau.devigetredditclient.home.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.util.IActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

/**
 * An activity representing a single RedditPost detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [PostListActivity].
 */
class PostDetailActivityWrapper(
    activity: IActivity
) {
    // Keeping a weak reference to the activity prevents a reference loop (and memory leak):
    private val weakActivity: WeakReference<IActivity> = WeakReference(activity)
    private val activity: IActivity?
        get() = weakActivity.get()

    fun onCreate(savedInstanceState: Bundle?) = activity?.apply {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // Show the Up button in the action bar.
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don"t need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = PostDetailFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(
                            PostDetailFragment.ARG_ITEM_ID,
                            getIntent().getStringExtra(PostDetailFragment.ARG_ITEM_ID)
                        )
                    }
                }


            getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, fragment)
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
    // class. That wrapper is just a regular class that can be instantiated easily, and contain all
    // Activity business logic. The actual Activity subclass is just a shell.
    private val activityWrapper = PostDetailActivityWrapper(this)
    override val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        setSupportActionBar(findViewById(R.id.detail_toolbar))

        activityWrapper.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem) = activityWrapper.onOptionsItemSelected(item)
}