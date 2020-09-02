package com.caiolandau.devigetredditclient.domain.repository

import com.caiolandau.devigetredditclient.domain.api.RedditApi
import com.caiolandau.devigetredditclient.domain.repository.converter.RedditPostsResponseToRedditPostsPageConverter

class RedditPostRepository(
    private val redditApi: RedditApi,
    private val converter: RedditPostsResponseToRedditPostsPageConverter = RedditPostsResponseToRedditPostsPageConverter()
) {
    suspend fun topPostsTodayPage(numOfItems: Int, after: String? = null, before: String? = null) =
        converter.convert(redditApi.getTopPostsTodayPage(numOfItems, after, before))
}