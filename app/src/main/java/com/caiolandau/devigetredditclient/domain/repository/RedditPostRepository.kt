package com.caiolandau.devigetredditclient.domain.repository

import android.util.Log
import com.caiolandau.devigetredditclient.domain.api.RedditApi
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.model.RedditPostPage
import com.caiolandau.devigetredditclient.domain.repository.converter.RedditPostsResponseToRedditPostsPageConverter

class RedditPostRepository(
    private val redditApi: RedditApi,
    private val converter: RedditPostsResponseToRedditPostsPageConverter = RedditPostsResponseToRedditPostsPageConverter()
) {
    private val localLoadedPosts = mutableListOf<RedditPost>()
    private val filteredPosts = mutableListOf<RedditPost>()

    suspend fun topPostsTodayPage(
        numOfItems: Int,
        after: String? = null,
        before: String? = null
    ): RedditPostPage {
        if (after == null && before == null && localLoadedPosts.isNotEmpty()) {
            // If we're loading the initial page and there is local data, this means we're simply
            // mutating the list. No need to re-load from the API in this case:
            return RedditPostPage(
                posts = localLoadedPosts.filter { !filteredPosts.contains(it) },
                pageAfter = localLoadedPosts.last().name,
                null
            )
        }
        Log.d("CFL", "Loading after: $after")
        val postPage = converter.convert(
            response = redditApi.getTopPostsTodayPage(numOfItems, after, before),
            filtering = filteredPosts
        )
        localLoadedPosts.addAll(postPage.posts)
        return postPage
    }

    fun invalidateLocalData() {
        // Ivnvalidating local data causes the next call to always "topPostsTodayPage" to reach the API:
        localLoadedPosts.clear()
    }

    fun filterPost(redditPost: RedditPost) = filteredPosts.add(redditPost)
}