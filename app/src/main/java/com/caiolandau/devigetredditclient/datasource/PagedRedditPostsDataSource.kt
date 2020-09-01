package com.caiolandau.devigetredditclient.datasource

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.repository.RedditPostRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PagedRedditPostsDataSource(
    private val repository: RedditPostRepository,
    private val coroutineScope: CoroutineScope,
    private val errorCallback: () -> Unit
) : PageKeyedDataSource<String, RedditPost>() {

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, RedditPost>
    ) {
        coroutineScope.launch {
            try {
                val postsPage = repository.topPostsTodayPage(params.requestedLoadSize)
                callback.onResult(postsPage.posts, postsPage.pageBefore, postsPage.pageAfter)
            } catch (exception: Exception) {
                errorCallback()
            }
        }
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, RedditPost>
    ) {
        coroutineScope.launch {
            try {
                val postsPage =
                    repository.topPostsTodayPage(params.requestedLoadSize, before = params.key)
                callback.onResult(postsPage.posts, postsPage.pageBefore)
            } catch (exception: Exception) {
                errorCallback()
            }
        }
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, RedditPost>
    ) {
        coroutineScope.launch {
            try {
                val postsPage =
                    repository.topPostsTodayPage(params.requestedLoadSize, after = params.key)
                callback.onResult(postsPage.posts, postsPage.pageAfter)
            } catch (exception: Exception) {
                errorCallback()
            }
        }
    }

    companion object {
        fun getFactory(
            redditPostRepository: RedditPostRepository,
            coroutineScope: CoroutineScope,
            errorCallback: () -> Unit
        ): Factory<String, RedditPost> {
            return object : DataSource.Factory<String, RedditPost>() {
                override fun create(): DataSource<String, RedditPost> {
                    return PagedRedditPostsDataSource(
                        redditPostRepository,
                        coroutineScope,
                        errorCallback
                    )
                }
            }
        }
    }
}