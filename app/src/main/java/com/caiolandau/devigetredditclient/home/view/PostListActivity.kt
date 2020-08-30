package com.caiolandau.devigetredditclient.home.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.dummy.DummyContent
import com.caiolandau.devigetredditclient.util.IActivity
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
    activity: IActivity
) {
    // Keeping a weak reference to the activity prevents a reference loop (and memory leak):
    private val weakActivity: WeakReference<IActivity> = WeakReference(activity)
    private val activity: IActivity?
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
        findViewById<RecyclerView>(R.id.item_list)?.apply {
            setupRecyclerView(this)
        }
    }


    private fun setupRecyclerView(recyclerView: RecyclerView) = activity?.apply {
        recyclerView.adapter =
            SimpleItemRecyclerViewAdapter(
                this,
                DummyContent.ITEMS,
                twoPane
            )
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: IActivity,
        private val values: List<DummyContent.DummyItem>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

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

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.findViewById(R.id.id_text)
            val contentView: TextView = view.findViewById(R.id.content)
        }
    }
}

class PostListActivity : AppCompatActivity(), IActivity {
    // In order to avoid needing something like Robolectric to test activity logic, we use a wrapper
    // class. That wrapper is just a regular class that can be instantiated easily, and contains all
    // Activity business logic. The actual Activity subclass is just a shell.
    private val activityWrapper = PostListActivityWrapper(this)
    override val context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)
        toolbar.title = title

        activityWrapper.onCreate(savedInstanceState)
    }
}