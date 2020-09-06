package com.caiolandau.devigetredditclient.domain.api

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.BufferedReader

@ExperimentalCoroutinesApi // Coroutines / Flow are still marked as experimental, although they are considered stable enough
@Suppress("BlockingMethodInNonBlockingContext") // This is fine in a unit test
class ApiUnitTests {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun test_redditApi_getTopPostsTodayPage_success() = runBlockingTest {
        mockWebServer.enqueue(MockResponse().setBody(readFileToString("reddit_posts_response.json")))
        mockWebServer.start()

        val baseUrl = mockWebServer.url("/top/")
        val subject = makeSubject(baseUrl.toString()).reddit

        val response = runBlocking {
            subject.getTopPostsTodayPage(30, "123_abc", null)
        }

        // Compares that the data matches what the response JSON has:
        assertEquals("mocked_after", response.data.after)
        assertEquals(3, response.data.children.size)
        assertEquals("First post's title", response.data.children.first().data.title)
    }

    @Test
    fun test_redditApi_getTopPostsTodayPage_failure() = runBlockingTest {
        // Mock an error response:
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.start()

        val baseUrl = mockWebServer.url("/top/")
        val subject = makeSubject(baseUrl.toString()).reddit

        try {
            runBlocking {
                subject.getTopPostsTodayPage(30, "123_abc", null)
            }
            // If the above didn't throw, the test failed (it should generate an error)
            fail("Request didn't throw!")
        } catch (ex: HttpException) {
            assertEquals(500, ex.code())
        }
    }

    private fun makeSubject(redditBaseUrl: String) = Api(redditBaseUrl = redditBaseUrl)

    private fun readFileToString(filename: String): String {
        val stream = javaClass.classLoader?.getResourceAsStream(filename)
            ?: throw Exception("Could not read JSON from $filename")

        return stream.bufferedReader().use(BufferedReader::readText)
    }
}