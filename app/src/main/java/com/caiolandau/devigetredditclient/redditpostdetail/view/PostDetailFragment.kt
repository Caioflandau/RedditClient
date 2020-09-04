package com.caiolandau.devigetredditclient.redditpostdetail.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostdetail.viewmodel.PostDetailViewModel
import kotlinx.coroutines.channels.sendBlocking

/**
 * A fragment representing a single post detail screen.
 * This fragment is either contained in a [PostListActivity] in two-pane mode (on tablets)
 * or a [PostDetailActivity] on handsets.
 */
class PostDetailFragment : Fragment() {

    lateinit var viewModel: PostDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val post = it.getParcelable<RedditPost>(ARG_POST) ?: return
            viewModel = getViewModel(post)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.post_detail, container, false)

        bindOutput(rootView)
        bindInput(rootView)

        return rootView
    }

    private fun bindOutput(rootView: View) {
        viewModel.output.postTitle
            .observe(viewLifecycleOwner) {
                rootView.findViewById<TextView>(R.id.txtPostTitle).text = it
            }

        viewModel.output.postText
            .observe(viewLifecycleOwner) {
                rootView.findViewById<TextView>(R.id.txtPostText).text = it
            }

        viewModel.output.postImageUrl
            .observe(viewLifecycleOwner) {
                rootView.findViewById<ImageView>(R.id.imgPostImage).load(it)
            }

        viewModel.output.openExternal
            .observe(viewLifecycleOwner) {
                val uri = it.getContentIfNotHandled() ?: return@observe
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context?.startActivity(intent)
            }

        viewModel.output.isOpenExternalButtonHidden
            .observe(viewLifecycleOwner) { isHidden ->
                val visibility = if (isHidden) View.GONE else View.VISIBLE
                rootView.findViewById<Button>(R.id.btnOpenExternal).visibility = visibility
            }
    }

    private fun bindInput(rootView: View) {
        rootView.findViewById<Button>(R.id.btnOpenExternal).setOnClickListener {
            viewModel.input.onClickOpenExternal.sendBlocking(Unit)
        }

        rootView.findViewById<Button>(R.id.btnOpenReddit).setOnClickListener {
            viewModel.input.onClickOpenReddit.sendBlocking(Unit)
        }
    }

    private fun getViewModel(post: RedditPost): PostDetailViewModel {
        // ViewModels are created once per scope (i.e. Fragment) and reused for as long as the scope
        // is alive. It's fine to use `by viewModels()` every time instead of holding an instance of
        // the ViewModel:
        val viewModel: PostDetailViewModel by viewModels(factoryProducer = { ViewModelFactory(post) })
        return viewModel
    }

    companion object {
        /**
         * The fragment argument representing the post that this fragment represents
         */
        const val ARG_POST = "post"
    }

    class ViewModelFactory(val post: RedditPost) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
                return PostDetailViewModel(post) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class") // Shouldn't happen
        }
    }
}