package com.caiolandau.devigetredditclient.datasource

import androidx.paging.PageKeyedDataSource
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.repository.RedditPostRepository

class PagedRedditPostsDataSource(
    private val repository: RedditPostRepository
) : PageKeyedDataSource<String, RedditPost>() {
    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, RedditPost>
    ) {
        repository.topPostsTodayPage(params.requestedLoadSize)
            .subscribe { postsPage ->
                callback.onResult(postsPage.posts, postsPage.pageBefore, postsPage.pageAfter)
            }
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, RedditPost>
    ) {
        repository.topPostsTodayPage(params.requestedLoadSize, before = params.key)
            .subscribe { postsPage ->
                callback.onResult(postsPage.posts, postsPage.pageBefore)
            }
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, RedditPost>
    ) {
        repository.topPostsTodayPage(params.requestedLoadSize, after = params.key)
            .subscribe { postsPage ->
                callback.onResult(postsPage.posts, postsPage.pageAfter)
            }
    }
}