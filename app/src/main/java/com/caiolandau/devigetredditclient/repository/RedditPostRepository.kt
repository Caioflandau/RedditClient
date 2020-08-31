package com.caiolandau.devigetredditclient.repository

import android.util.Log
import com.caiolandau.devigetredditclient.api.RedditApi
import com.caiolandau.devigetredditclient.home.model.RedditPostPage
import com.caiolandau.devigetredditclient.repository.converter.RedditPostsResponseToRedditPostsPageConverter
import io.reactivex.rxjava3.core.Single

class RedditPostRepository(
    private val redditApi: RedditApi,
    private val converter: RedditPostsResponseToRedditPostsPageConverter = RedditPostsResponseToRedditPostsPageConverter()
) {
    fun topPostsTodayPage(numOfItems: Int, after: String? = null): Single<RedditPostPage> =
        redditApi.getTopPosts(numOfItems, "day")
            .map(converter::convert)
}