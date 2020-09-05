package com.caiolandau.devigetredditclient.redditpostdetail.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.Coil
import coil.ImageLoader
import coil.load
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostdetail.viewmodel.PostDetailViewModel
import com.caiolandau.devigetredditclient.util.LocalImageSaver
import com.caiolandau.devigetredditclient.util.SnackbarHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.channels.sendBlocking
import java.lang.IllegalStateException
import java.lang.ref.WeakReference

/**
 * A fragment representing a single post detail screen.
 * This fragment is either contained in a [PostListActivity] in two-pane mode (on tablets)
 * or a [PostDetailActivity] on handsets.
 */

interface IFragment {
    val ctx: Context?
    val args: Bundle?
    val viewLifecycleOwner: LifecycleOwner
    fun getViewModel(post: RedditPost): PostDetailViewModel
}

class PostDetailFragmentWrapper(
    fragment: IFragment,
    private val imageLoader: ImageLoader,
    private val localImageSaver: LocalImageSaver = LocalImageSaver(),
    private val snackbarHelper: SnackbarHelper = SnackbarHelper()
) {
    // Keeping a weak reference to the fragment prevents a reference loop (and memory leak):
    private val weakFragment: WeakReference<IFragment> = WeakReference(fragment)
    private val fragment: IFragment?
        get() = weakFragment.get()

    lateinit var viewModel: PostDetailViewModel

    fun onCreate(savedInstanceState: Bundle?) = fragment?.apply {
        args?.let {
            val post = it.getParcelable<RedditPost>(PostDetailFragment.ARG_POST) ?: return this
            viewModel = getViewModel(post)
        }
    }

    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.post_detail, container, false)

        bindOutput(rootView)
        bindInput(rootView)

        return rootView
    }

    private fun bindOutput(rootView: View) = fragment?.apply {
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
                rootView.findViewById<ImageView>(R.id.imgPostImage).load(it, imageLoader) {
                    listener(onSuccess = { _, _ ->
                        viewModel.input.onImageLoadedSuccessfully.sendBlocking(Unit)
                    }, onError = { _, _ ->
                        viewModel.input.onErrorLoadingImage.sendBlocking(Unit)
                    })
                }
            }

        viewModel.output.openExternal
            .observe(viewLifecycleOwner) {
                val uri = it.getContentIfNotHandled() ?: return@observe
                val intent = Intent(Intent.ACTION_VIEW, uri)
                ctx?.startActivity(intent)
            }

        viewModel.output.isSaveImageButtonHidden
            .observe(viewLifecycleOwner) { isHidden ->
                val visibility = if (isHidden) View.GONE else View.VISIBLE
                rootView.findViewById<Button>(R.id.btnSaveImage).visibility = visibility
            }

        viewModel.output.saveImageToGallery
            .observe(viewLifecycleOwner) { filenameEvent ->
                saveImageToGallery(
                    filename = filenameEvent.getContentIfNotHandled() ?: return@observe,
                    rootView = rootView
                )
            }

        viewModel.output.isProgressBarHidden
            .observe(viewLifecycleOwner) {
                val visibility = if (it) View.GONE else View.VISIBLE
                rootView.findViewById<ProgressBar>(R.id.progressLoadingImage).visibility =
                    visibility
            }
    }

    private fun saveImageToGallery(filename: String, rootView: View) {
        val drawable = rootView.findViewById<ImageView>(R.id.imgPostImage).drawable ?: return
        val context = fragment?.ctx ?: return
        localImageSaver.saveImageToGallery(context, filename, drawable)
        snackbarHelper.showSnackbar(rootView, R.string.image_saved_success_message)
    }

    private fun bindInput(rootView: View) {
        rootView.findViewById<Button>(R.id.btnOpenExternal).setOnClickListener {
            viewModel.input.onClickOpenExternal.sendBlocking(Unit)
        }

        rootView.findViewById<Button>(R.id.btnOpenReddit).setOnClickListener {
            viewModel.input.onClickOpenReddit.sendBlocking(Unit)
        }

        rootView.findViewById<Button>(R.id.btnSaveImage).setOnClickListener {
            viewModel.input.onClickSaveImage.sendBlocking(Unit)
        }
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

class PostDetailFragment : Fragment(), IFragment {
    // In order to avoid needing something like Robolectric to test fragment logic, we use a wrapper
    // class. That wrapper is just a regular class that can be instantiated easily, and contains all
    // Fragment business logic. The actual Fragment subclass is just a shell.
    private val fragmentWrapper: PostDetailFragmentWrapper by lazy {
        val ctx = context ?: throw IllegalStateException("No context when trying to create wrapper!")
        PostDetailFragmentWrapper(this, Coil.imageLoader(ctx))
    }

    override val ctx
        get() = context
    override val args: Bundle?
        get() = arguments
    override val viewLifecycleOwner = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentWrapper.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return fragmentWrapper.onCreateView(
            inflater,
            container,
            savedInstanceState
        )
    }

    override fun getViewModel(post: RedditPost): PostDetailViewModel {
        // ViewModels are created once per scope (i.e. Fragment) and reused for as long as the scope
        // is alive. It's fine to use `by viewModels()` every time instead of holding an instance of
        // the ViewModel:
        val viewModel: PostDetailViewModel by viewModels(factoryProducer = {
            PostDetailFragmentWrapper.ViewModelFactory(
                post
            )
        })
        return viewModel
    }

    companion object {
        /**
         * The fragment argument representing the post that this fragment represents
         */
        const val ARG_POST = "post"
    }
}