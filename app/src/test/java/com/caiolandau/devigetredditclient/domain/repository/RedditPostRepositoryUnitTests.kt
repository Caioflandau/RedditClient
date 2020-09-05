package com.caiolandau.devigetredditclient.domain.repository

import com.caiolandau.devigetredditclient.domain.api.RedditApi
import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponse
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.model.RedditPostPage
import com.caiolandau.devigetredditclient.domain.repository.converter.RedditPostsResponseToRedditPostsPageConverter
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import retrofit2.HttpException
import java.util.*

class RedditPostRepositoryUnitTests {

    @MockK
    private lateinit var mockRedditApi: RedditApi

    @MockK
    private lateinit var mockConverter: RedditPostsResponseToRedditPostsPageConverter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun topPostsTodayPage_page_success() = runBlockingTest {
        val subject = makeSubject()

        val mockResponse = mockk<RedditPostsResponse>()
        coEvery { mockRedditApi.getTopPostsTodayPage(10) } returns mockResponse

        val post1 = mockRedditPost()
        val post2 = mockRedditPost()

        val mockRedditPostPage = mockk<RedditPostPage>()
        every { mockRedditPostPage.pageAfter } returns "the_after"
        every { mockRedditPostPage.pageBefore } returns null
        every { mockRedditPostPage.posts } returns listOf(post1, post2)
        every { mockConverter.convert(mockResponse) } returns mockRedditPostPage

        val resultPage = subject.topPostsTodayPage(10, null, null)
        val expectedPage = RedditPostPage(
            posts = resultPage.posts,

            // If we reached the max number of posts allowed, we should stop loading more pages.
            // This means simply passing `null` as the "pageAfter":
            pageAfter = "the_after",
            pageBefore = null
        )

        assertEquals(expectedPage.pageBefore, resultPage.pageBefore)
        assertEquals(expectedPage.pageAfter, resultPage.pageAfter)
        assertEquals(expectedPage.posts, resultPage.posts)
    }

    @Test
    fun topPostsTodayPage_pageFails() = runBlockingTest {
        val subject = makeSubject()

        val mockException = mockk<HttpException>()
        coEvery { mockRedditApi.getTopPostsTodayPage(15) } throws mockException

        try {
            val resultPage = subject.topPostsTodayPage(15, null, null)
            fail("Expected call to throw but it didn't")
        } catch (ex: HttpException) {
            assertEquals(mockException, ex)
        }
    }

    @Test
    fun topPostsTodayPage_page_withFilter() = runBlockingTest {
        val subject = makeSubject()

        val mockResponse = mockk<RedditPostsResponse>()
        coEvery { mockRedditApi.getTopPostsTodayPage(10, after = "the_after") } returns mockResponse

        val post1 = mockRedditPost()
        val post2 = mockRedditPost()

        val mockRedditPostPage = mockk<RedditPostPage>()
        every { mockRedditPostPage.pageAfter } returns "the_more_after"
        every { mockRedditPostPage.pageBefore } returns "the_before"
        every { mockRedditPostPage.posts } returns listOf(post1, post2)
        every { mockConverter.convert(mockResponse) } returns mockRedditPostPage

        // Add the filter:
        subject.filterPost(post2)

        val resultPage = subject.topPostsTodayPage(10, "the_after", null)

        val expectedPage = RedditPostPage(
            posts = listOf(post1),

            // If we reached the max number of posts allowed, we should stop loading more pages.
            // This means simply passing `null` as the "pageAfter":
            pageAfter = "the_more_after",
            pageBefore = "the_before"
        )

        assertEquals(expectedPage.pageBefore, resultPage.pageBefore)
        assertEquals(expectedPage.pageAfter, resultPage.pageAfter)
        assertEquals(expectedPage.posts, resultPage.posts)
    }

    @Test
    fun topPostsTodayPage_page_withRead() = runBlockingTest {
        val subject = makeSubject()

        val mockResponse = mockk<RedditPostsResponse>()
        coEvery { mockRedditApi.getTopPostsTodayPage(10, before = "the_before") } returns mockResponse

        val post1 = mockRedditPost()
        val post2 = mockRedditPost()

        val mockRedditPostPage = mockk<RedditPostPage>()
        every { mockRedditPostPage.pageAfter } returns null
        every { mockRedditPostPage.pageBefore } returns "the_before"
        every { mockRedditPostPage.posts } returns listOf(post1, post2)
        every { mockConverter.convert(mockResponse) } returns mockRedditPostPage

        // Mark a post as read:
        subject.markPostAsRead(post1)

        val resultPage = subject.topPostsTodayPage(10, null, "the_before")

        val expectedPage = RedditPostPage(
            posts = listOf(post1.apply { isRead = true }, post2),

            // If we reached the max number of posts allowed, we should stop loading more pages.
            // This means simply passing `null` as the "pageAfter":
            pageAfter = null,
            pageBefore = "the_before"
        )

        assertEquals(expectedPage.pageBefore, resultPage.pageBefore)
        assertEquals(expectedPage.pageAfter, resultPage.pageAfter)
        assertEquals(expectedPage.posts, resultPage.posts)
    }

    @Test
    fun topPostsTodayPage_page_withFilterAndRead() = runBlockingTest {
        val subject = makeSubject()

        val mockResponse = mockk<RedditPostsResponse>()
        coEvery { mockRedditApi.getTopPostsTodayPage(10, after = "the_after") } returns mockResponse

        val post1 = mockRedditPost()
        val post2 = mockRedditPost()
        val post3 = mockRedditPost()
        val post4 = mockRedditPost()

        val mockRedditPostPage = mockk<RedditPostPage>()
        every { mockRedditPostPage.pageAfter } returns "the_more_after"
        every { mockRedditPostPage.pageBefore } returns "the_before"
        every { mockRedditPostPage.posts } returns listOf(post1, post2, post3, post4)
        every { mockConverter.convert(mockResponse) } returns mockRedditPostPage

        // Add the filters:
        subject.filterPost(post1)
        subject.filterPost(post3)

        // Mark a post as read:
        subject.markPostAsRead(post4)

        val resultPage = subject.topPostsTodayPage(10, "the_after", null)

        val expectedPage = RedditPostPage(
            posts = listOf(post2, post4.apply { isRead = true }),

            // If we reached the max number of posts allowed, we should stop loading more pages.
            // This means simply passing `null` as the "pageAfter":
            pageAfter = "the_more_after",
            pageBefore = "the_before"
        )

        assertEquals(expectedPage.pageBefore, resultPage.pageBefore)
        assertEquals(expectedPage.pageAfter, resultPage.pageAfter)
        assertEquals(expectedPage.posts, resultPage.posts)
    }

    @Test
    fun topPostsTodayPage_page_reloadFromLocal() = runBlockingTest {
        val subject = makeSubject()

        val mockResponse = mockk<RedditPostsResponse>()
        coEvery { mockRedditApi.getTopPostsTodayPage(10) } returns mockResponse

        val post1 = mockRedditPost()
        val post2 = mockRedditPost()
        val post3 = mockRedditPost()
        val post4 = mockRedditPost()

        val mockRedditPostPage = mockk<RedditPostPage>()
        every { mockRedditPostPage.pageAfter } returns "the_more_after"
        every { mockRedditPostPage.pageBefore } returns "the_before"
        every { mockRedditPostPage.posts } returns listOf(post1, post2, post3, post4)
        every { mockConverter.convert(mockResponse) } returns mockRedditPostPage

        // First call goes through the API:
        subject.topPostsTodayPage(10)

        // Second call uses the local copy:
        val resultPageFromLocal = subject.topPostsTodayPage(10)

        val expectedPage = RedditPostPage(
            posts = listOf(post1, post2, post3, post4),
            pageAfter = post4.name,
            pageBefore = null
        )

        // Should only call the API once - the second call is a local fetch:
        coVerify(exactly = 1) { mockRedditApi.getTopPostsTodayPage(any(), any(), any()) }

        assertEquals(expectedPage.pageBefore, resultPageFromLocal.pageBefore)
        assertEquals(expectedPage.pageAfter, resultPageFromLocal.pageAfter)
        assertEquals(expectedPage.posts, resultPageFromLocal.posts)
    }

    @Test
    fun topPostsTodayPage_page_stopsWhenReachLimit() = runBlockingTest {
        val subject = makeSubject(maxPosts = 50)

        val mockResponse1 = mockk<RedditPostsResponse>()
        coEvery { mockRedditApi.getTopPostsTodayPage(25) } returns mockResponse1

        val mockResponse2 = mockk<RedditPostsResponse>()
        coEvery { mockRedditApi.getTopPostsTodayPage(25, after = "the_after") } returns mockResponse2

        // Add 25 posts to the first response:
        val posts1 = mutableListOf<RedditPost>().apply {
            repeat(25) {
                add(mockRedditPost())
            }
        }

        val mockRedditPostPage1 = mockk<RedditPostPage>()
        every { mockRedditPostPage1.pageAfter } returns "the_after"
        every { mockRedditPostPage1.pageBefore } returns "the_before"
        every { mockRedditPostPage1.posts } returns posts1
        every { mockConverter.convert(mockResponse1) } returns mockRedditPostPage1

        val mockRedditPostPage2 = mockk<RedditPostPage>()
        every { mockRedditPostPage2.pageAfter } returns "the_more_after"
        every { mockRedditPostPage2.pageBefore } returns "the_even_before"
        every { mockRedditPostPage2.posts } returns posts1
        every { mockConverter.convert(mockResponse2) } returns mockRedditPostPage2

        // First call returns 25 posts:
        subject.topPostsTodayPage(25)

        // Second call returns another 25 posts:
        val resultSecondCall = subject.topPostsTodayPage(25, after = mockRedditPostPage1.pageAfter)

        // `pageAfter` should be null to stop loading more pages:
        assertNull(resultSecondCall.pageAfter)
    }

    private fun mockRedditPost() = RedditPost(
        id = UUID.randomUUID().toString(),
        name = UUID.randomUUID().toString(),
        title = UUID.randomUUID().toString(),
        author = UUID.randomUUID().toString(),
        selfText = UUID.randomUUID().toString(),
        permalink = UUID.randomUUID().toString(),
        entryDate = "12/12/2020",
        imageUrl = "https://some-image.com/image.png",
        thumbnailUrl = "https://some-image.com/thumb.png",
        numOfComments = Random().nextInt(),
        isRead = false
    )

    private fun makeSubject(maxPosts: Int = 50): RedditPostRepository {
        return RedditPostRepository(mockRedditApi, mockConverter, maxPosts)
    }
}