package com.caiolandau.devigetredditclient.redditpostdetail.view

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.metadata
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostdetail.viewmodel.PostDetailViewModel
import com.caiolandau.devigetredditclient.util.Event
import com.google.android.material.snackbar.Snackbar
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
                rootView.findViewById<ImageView>(R.id.imgPostImage).load(it) {
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
                context?.startActivity(intent)
            }

        viewModel.output.isSaveImageButtonHidden
            .observe(viewLifecycleOwner) { isHidden ->
                val visibility = if (isHidden) View.GONE else View.VISIBLE
                rootView.findViewById<Button>(R.id.btnSaveImage).visibility = visibility
            }

        viewModel.output.saveImageToGallery
            .observe(viewLifecycleOwner) { filename ->
                val filename = filename.getContentIfNotHandled() ?: return@observe
                saveImageToGallery(filename, rootView)
            }

        viewModel.output.isProgressBarHidden
            .observe(viewLifecycleOwner) {
                val visibility = if (it) View.GONE else View.VISIBLE
                rootView.findViewById<ProgressBar>(R.id.progressLoadingImage).visibility = visibility
            }
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

    private fun saveImageToGallery(
        filename: String,
        rootView: View
    ) {
        val resolver = context?.applicationContext?.contentResolver ?: return
        val drawable = rootView.findViewById<ImageView>(R.id.imgPostImage).drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        }
        val imageCollection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageContentUri = resolver.insert(imageCollection, imageDetails) ?: return
        val out = resolver.openOutputStream(imageContentUri, "w")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out?.close()
        Snackbar.make(rootView, R.string.image_saved_success_message, Snackbar.LENGTH_LONG)
            .show()
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