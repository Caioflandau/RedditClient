package com.caiolandau.devigetredditclient.domain.datasource

import androidx.paging.PageKeyedDataSource
import androidx.paging.PositionalDataSource
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.model.RedditPostPage
import com.caiolandau.devigetredditclient.domain.repository.RedditPostRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class PagedRedditPostsDataSourceUnitTests {
    @MockK private lateinit var mockRepo: RedditPostRepository
    @MockK private lateinit var mockOnErrorCallback: () -> Unit

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun test_loadInitial_success() = runBlockingTest {
        val subject = makeSubject(this)
        val params = PageKeyedDataSource.LoadInitialParams<String>(30, false)
        val callback: PageKeyedDataSource.LoadInitialCallback<String, RedditPost> = mockk(relaxed = true)

        // When request is successful:
        val expectedResponse = RedditPostPage(listOf(mockk(), mockk()), "aaa_bbb", null)
        coEvery {
            mockRepo.topPostsTodayPage(30, null, null)
        } returns expectedResponse

        subject.loadInitial(params, callback)

        // Should call the callback with the loaded page:
        verify { callback.onResult(expectedResponse.posts, expectedResponse.pageBefore, expectedResponse.pageAfter) }
    }

    @Test
    fun test_loadInitial_failure() = runBlockingTest {
        val subject = makeSubject(this)
        val params = PageKeyedDataSource.LoadInitialParams<String>(30, false)
        val callback: PageKeyedDataSource.LoadInitialCallback<String, RedditPost> = mockk(relaxed = true)

        // When request fails
        coEvery {
            mockRepo.topPostsTodayPage(30, null, null)
        } throws Exception()

        subject.loadInitial(params, callback)

        // Should NOT call the callback
        confirmVerified(callback)

        // Should call the error callback
        verify { mockOnErrorCallback.invoke() }
    }

    @Test
    fun test_loadBefore_success() = runBlockingTest {
        val subject = makeSubject(this)
        val params = PageKeyedDataSource.LoadParams<String>("aaa_bbb", 10)
        val callback: PageKeyedDataSource.LoadCallback<String, RedditPost> = mockk(relaxed = true)

        // When request is successful:
        val expectedResponse = RedditPostPage(listOf(mockk(), mockk(), mockk()), "bbb_ccc", "aaa_bbb")
        coEvery {
            mockRepo.topPostsTodayPage(10, "aaa_bbb", null)
        } returns expectedResponse

        subject.loadBefore(params, callback)

        // Should call the callback with the loaded page:
        verify { callback.onResult(expectedResponse.posts, expectedResponse.pageAfter) }
    }

    @Test
    fun test_loadBefore_failure() = runBlockingTest {
        val subject = makeSubject(this)
        val params = PageKeyedDataSource.LoadInitialParams<String>(30, false)
        val callback: PageKeyedDataSource.LoadInitialCallback<String, RedditPost> = mockk(relaxed = true)

        // When request fails
        coEvery {
            mockRepo.topPostsTodayPage(30, null, null)
        } throws Exception()

        subject.loadInitial(params, callback)

        // Should NOT call the callback
        confirmVerified(callback)

        // Should call the error callback
        verify { mockOnErrorCallback.invoke() }
    }

    @Test
    fun test_loadAfter() {
    }

    fun makeSubject(scope: CoroutineScope): PagedRedditPostsDataSource {
        return PagedRedditPostsDataSource(mockRepo, scope, mockOnErrorCallback)
    }
}