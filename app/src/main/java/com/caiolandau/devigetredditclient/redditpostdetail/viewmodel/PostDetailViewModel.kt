package com.caiolandau.devigetredditclient.redditpostdetail.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.util.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*

class PostDetailViewModel(
    post: RedditPost
) : ViewModel() {
    /**
     * Represents input events - i.e. button clicks - that are possible from the view:
     */
    class Input {
        val onClickOpenExternal: BroadcastChannel<Unit> = BroadcastChannel(1)
        val onClickOpenReddit: BroadcastChannel<Unit> = BroadcastChannel(1)
        val onImageLoadedSuccessfully: BroadcastChannel<Unit> = BroadcastChannel(1)
        val onClickSaveImage: BroadcastChannel<Unit> = BroadcastChannel(1)
        val onErrorLoadingImage: BroadcastChannel<Unit> = BroadcastChannel(1)
    }

    /**
     * Represents outputs - i.e. posts title - to be presented/handled by the view
     */
    class Output(
        val postTitle: LiveData<String>,
        val postImageUrl: LiveData<String?>,
        val postText: LiveData<String>,
        val openExternal: LiveData<Event<Uri>>,
        val isSaveImageButtonHidden: LiveData<Boolean>,
        val saveImageToGallery: LiveData<Event<String>>,
        val isProgressBarHidden: LiveData<Boolean>
    )

    val input = Input()
    val output: Output by lazy {
        Output(
            postTitle = MutableLiveData(post.title),
            postImageUrl = initPostImageUrl(post),
            postText = MutableLiveData(post.selfText),
            openExternal = initOpenMediaExternal(post),
            isSaveImageButtonHidden = initIsSaveImageButtonHidden(),
            saveImageToGallery = initSaveImageToGallery(post),
            isProgressBarHidden = iniIsProgressBarHidden()
        )
    }

    private fun initPostImageUrl(post: RedditPost): LiveData<String?> {
        // Tries the main "URL" in the post, which can be the full-size image if one is available.
        // If that fails (i.e. it's not an image), falls back to the thumbnail instead:
        val imageUrlFlow = flow { emit(post.imageUrl) }
        val errorLoadingImageFlow = input.onErrorLoadingImage.asFlow()
            .map { post.thumbnailUrl }

        return merge(imageUrlFlow, errorLoadingImageFlow)
            .asLiveData(viewModelScope.coroutineContext)
    }

    private fun iniIsProgressBarHidden() = merge(
        input.onImageLoadedSuccessfully.asFlow()
            .map { true },

        input.onErrorLoadingImage.asFlow()
            .map { true }
    ).asLiveData(viewModelScope.coroutineContext)

    private fun initOpenMediaExternal(post: RedditPost) = merge(
        input.onClickOpenExternal.asFlow()
            .map { post }
            .filter { it.imageUrl != null }
            .map { Uri.parse(it.imageUrl!!) } // Force-unwrapping is safe because we filter above
            .map(::Event),

        input.onClickOpenReddit.asFlow()
            .map { "$REDDIT_BASE_URL${post.permalink}" }
            .map { Uri.parse(it) }
            .map(::Event)
    ).asLiveData(viewModelScope.coroutineContext)

    private fun initIsSaveImageButtonHidden() = merge(
        input.onImageLoadedSuccessfully.asFlow()
            .map { false },

        input.onErrorLoadingImage.asFlow()
            .map { true }
    ).onStart { emit(true) }.asLiveData(viewModelScope.coroutineContext)

    private fun initSaveImageToGallery(post: RedditPost) = input.onClickSaveImage.asFlow()
        .map { "reddit-${post.name}" }
        .map(::Event)
        .asLiveData(viewModelScope.coroutineContext)

    override fun onCleared() {
        super.onCleared()
        input.onClickOpenExternal.close()
        input.onClickOpenReddit.close()
        input.onImageLoadedSuccessfully.close()
        input.onClickSaveImage.close()
        input.onErrorLoadingImage.close()
    }

    private companion object {
        const val REDDIT_BASE_URL = "https://reddit.com"
    }
}