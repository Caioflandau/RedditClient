package com.caiolandau.devigetredditclient.redditpostdetail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.util.Event
import kotlinx.coroutines.channels.Channel

class PostDetailViewModel(
    post: RedditPost
): ViewModel() {
    /**
     * Represents input events - i.e. button clicks - that are possible from the view:
     */
    class Input {

    }

    /**
     * Represents outputs - i.e. posts title - to be presented/handled by the view
     */
    class Output(
        val postTitle: LiveData<String>,
        val postImageUrl: LiveData<String>,
        val postText: LiveData<String>
    )

    val output = Output(
        postTitle = MutableLiveData(post.title),
        postImageUrl = MutableLiveData(post.imageUrl),
        postText = MutableLiveData(post.selfText)
    )
}