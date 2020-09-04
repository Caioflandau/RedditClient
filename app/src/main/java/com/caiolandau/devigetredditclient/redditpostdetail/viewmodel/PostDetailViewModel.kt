package com.caiolandau.devigetredditclient.redditpostdetail.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.util.Event
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class PostDetailViewModel(
    post: RedditPost
) : ViewModel() {
    /**
     * Represents input events - i.e. button clicks - that are possible from the view:
     */
    class Input {
        val onClickOpenExternal: BroadcastChannel<Unit> = BroadcastChannel(1)
        val onClickOpenReddit: BroadcastChannel<Unit> = BroadcastChannel(1)
    }

    /**
     * Represents outputs - i.e. posts title - to be presented/handled by the view
     */
    class Output(
        val postTitle: LiveData<String>,
        val postImageUrl: LiveData<String>,
        val postText: LiveData<String>,
        val openExternal: LiveData<Event<Uri>>,
        val isOpenExternalButtonHidden: LiveData<Boolean>
    )

    val input = Input()
    val output = Output(
        postTitle = MutableLiveData(post.title),
        postImageUrl = MutableLiveData(post.imageUrl),
        postText = MutableLiveData(post.selfText),
        openExternal = initOpenMediaExternal(post),
        isOpenExternalButtonHidden = MutableLiveData(post.imageUrl == null)
    )

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

    private companion object {
        const val REDDIT_BASE_URL = "https://reddit.com"
    }
}