package com.caiolandau.devigetredditclient.domain.repository

import com.caiolandau.devigetredditclient.domain.api.RedditApi
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.model.RedditPostPage
import com.caiolandau.devigetredditclient.domain.repository.converter.RedditPostsResponseToRedditPostsPageConverter

class RedditPostRepository(
    private val redditApi: RedditApi,
    private val converter: RedditPostsResponseToRedditPostsPageConverter = RedditPostsResponseToRedditPostsPageConverter()
) {
    private val localData = mutableListOf<RedditPost>()

    suspend fun topPostsTodayPage(
        numOfItems: Int,
        after: String? = null,
        before: String? = null
    ): RedditPostPage {
        if (after == null && before == null && localData.isNotEmpty()) {
            // If we're loading the initial page and there is local data, this means we're simply
            // mutating the list. No need to re-load from the API in this case:
            return RedditPostPage(posts = localData, pageAfter = localData.last().name, null)
        }
        val postPage = converter.convert(redditApi.getTopPostsTodayPage(numOfItems, after, before))
        localData.addAll(postPage.posts)
        return postPage
    }

    fun invalidateLocalData() {
        // Ivnvalidating local data causes the next call to always "topPostsTodayPage" to reach the API:
        localData.clear()
    }
}