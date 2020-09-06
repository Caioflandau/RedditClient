package com.caiolandau.devigetredditclient.domain.repository.converter

import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponse
import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponseChild
import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponseData
import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponsePostData
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.*
import kotlin.random.Random

class RedditPostsResponseToRedditPostsPageConverterUnitTests {

    @MockK
    private lateinit var mockEpochConverter: EpochToRelativeTimeConverter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockEpochConverter.convert(any(), any(), any()) } returns "12 hours ago"
    }

    @Test
    fun test_convert() {
        val subject = RedditPostsResponseToRedditPostsPageConverter(mockEpochConverter)
        val mockResponse = createMockResponse()

        val result = subject.convert(mockResponse)

        assertEquals(mockResponse.data.after, result.pageAfter)
        assertEquals(mockResponse.data.before, result.pageBefore)
        assertEquals(mockResponse.data.children.size, result.posts.size)
        result.posts.forEachIndexed { index, post ->
            assertEquals(expectedPostAtIndex(index, mockResponse), post)
        }
    }

    private fun expectedPostAtIndex(index: Int, mockResponse: RedditPostsResponse) =
        mockResponse.data.children[index].data.let {
            RedditPost(
                id = it.id,
                name = it.name,
                title = it.title,
                author = it.author,
                selfText = it.selfText,
                permalink = it.permalink,
                entryDate = "12 hours ago",
                url = it.url,
                thumbnailUrl = it.thumbnail,
                numOfComments = it.numComments,
                isRead = false
            )
        }

    private fun createMockResponse() = RedditPostsResponse(
        kind = "t3",
        data = RedditPostsResponseData(
            children = mockRedditPostsResponseChildren(),
            after = "aaa_bbb",
            before = null
        )
    )

    private fun mockRedditPostsResponseChildren() = mutableListOf<RedditPostsResponseChild>().apply {
        add(RedditPostsResponseChild(data = mockRedditPostsResponsePostData()))
        add(RedditPostsResponseChild(data = mockRedditPostsResponsePostData()))
        add(RedditPostsResponseChild(data = mockRedditPostsResponsePostData()))
        add(RedditPostsResponseChild(data = mockRedditPostsResponsePostData()))
        add(RedditPostsResponseChild(data = mockRedditPostsResponsePostData()))
    }

    private fun mockRedditPostsResponsePostData() = RedditPostsResponsePostData(
        id = UUID.randomUUID().toString(),
        name = UUID.randomUUID().toString(),
        title = UUID.randomUUID().toString(),
        selfText = UUID.randomUUID().toString(),
        permalink = UUID.randomUUID().toString(),
        author = UUID.randomUUID().toString(),
        thumbnail = UUID.randomUUID().toString(),
        createdUtc = Date().time,
        url = UUID.randomUUID().toString(),
        numComments = Random.nextInt(),
        preview = null
    )
}