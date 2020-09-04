package com.caiolandau.devigetredditclient.domain.repository

import com.caiolandau.devigetredditclient.domain.api.RedditApi
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.model.RedditPostPage
import com.caiolandau.devigetredditclient.domain.repository.converter.RedditPostsResponseToRedditPostsPageConverter

class RedditPostRepository(
    private val redditApi: RedditApi,
    private val converter: RedditPostsResponseToRedditPostsPageConverter = RedditPostsResponseToRedditPostsPageConverter(),

    // Note: ideally, as a Reddit client, we'd load indefinitely.
    // For this exercise, we stop at 50 as explicitly requested in the assignment
    private val maxPosts: Int = 50
) {
    private val localLoadedPosts = mutableListOf<RedditPost>()
    private val filteredPosts = mutableListOf<RedditPost>()
    private val readPosts = mutableSetOf<RedditPost>()

    suspend fun topPostsTodayPage(
        numOfItems: Int,
        after: String? = null,
        before: String? = null
    ): RedditPostPage {
        if (after == null && before == null && localLoadedPosts.isNotEmpty()) {
            // If we're loading the initial page and there is local data, this means we're simply
            // mutating the list. No need to re-load from the API in this case:
            return RedditPostPage(
                posts = localLoadedPosts.byFiltering(filteredPosts).byApplyingReadSet(readPosts),
                pageAfter = if (hasReachedLimitOfPosts()) null else localLoadedPosts.last().name,
                pageBefore = null
            )
        }

        val postPage = converter.convert(
            response = redditApi.getTopPostsTodayPage(numOfItems, after, before)
        )
        localLoadedPosts.addAll(postPage.posts)

        return RedditPostPage(
            posts = postPage.posts.byFiltering(filteredPosts).byApplyingReadSet(readPosts),

            // If we reached the max number of posts allowed, we should stop loading more pages.
            // This means simply passing `null` as the "pageAfter":
            pageAfter = if (hasReachedLimitOfPosts()) null else postPage.pageAfter,
            pageBefore = postPage.pageBefore
        )
    }

    fun filterPost(redditPost: RedditPost) = filteredPosts.add(redditPost)

    fun markPostAsRead(post: RedditPost) = readPosts.add(post)

    fun invalidateLocalData() {
        // Invalidating local data causes the next call to always "topPostsTodayPage" to reach the API:
        localLoadedPosts.clear()
    }

    private fun hasReachedLimitOfPosts() = localLoadedPosts.count() >= maxPosts

    // Some utility extensions to apply repeated logic in lists of RedditPosts:
    private fun List<RedditPost>.byFiltering(filterList: List<RedditPost>) = this.filter { post ->
        !filteredPosts.map { it.id }.contains(post.id)
    }
    private fun List<RedditPost>.byApplyingReadSet(readList: Set<RedditPost>) = this.map { post ->
        post.withReadStatus(readList.map { it.id }.contains(post.id))
    }
    private fun RedditPost.withReadStatus(readStatus: Boolean) = this.apply { isRead = readStatus }
}