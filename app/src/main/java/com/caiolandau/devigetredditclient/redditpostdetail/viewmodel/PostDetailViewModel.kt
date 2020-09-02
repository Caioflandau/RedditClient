package com.caiolandau.devigetredditclient.redditpostdetail.viewmodel

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.util.Event
import kotlinx.coroutines.channels.Channel

class PostDetailViewModel(
    post: RedditPost
) {
    /**
     * Represents input events - i.e. list item clicks - that are possible from the view:
     */
    class Input {

    }

    /**
     * Represents outputs - i.e. list of posts - to be presented/handled by the view
     */
    class Output(

    )
}