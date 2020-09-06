package com.caiolandau.devigetredditclient.redditpostdetail.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.test_utils.makeRedditPost
import com.caiolandau.devigetredditclient.util.Event
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PostDetailViewModelUnitTests {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testCoroutineDispatcher)
        mockkStatic(Uri::class)
    }

    @Test
    fun test_postTitle() = runBlockingTest {
        val subject = makeSubject(makeRedditPost(title = "This is the title"))
        assertEquals("This is the title", subject.output.postTitle.value)
    }

    @Test
    fun test_postImageUrl() = runBlockingTest {
        val subject = makeSubject(makeRedditPost(url = "https://aaa.com/bbb.jpg"))
        val observer: (t: String?) -> Unit = {}
        subject.output.postImageUrl.observeForever(observer)

        assertEquals("https://aaa.com/bbb.jpg", subject.output.postImageUrl.value)

        subject.output.postImageUrl.removeObserver(observer)
    }

    @Test
    fun test_postImageUrl_whenImageFails_fallbackToThumbnail() = runBlockingTest {
        val subject = makeSubject(
            makeRedditPost(
                url = "https://aaa.com/bbb.jpg",
                thumbnailUrl = "https://thumb.com/image.jpg"
            )
        )
        val observer: (t: String?) -> Unit = {}
        subject.output.postImageUrl.observeForever(observer)
        subject.input.onErrorLoadingImage.send(Unit)

        assertEquals("https://thumb.com/image.jpg", subject.output.postImageUrl.value)

        subject.output.postImageUrl.removeObserver(observer)
    }

    @Test
    fun test_postText() = runBlockingTest {
        val subject = makeSubject(makeRedditPost(selfText = "This is the self-text"))
        assertEquals("This is the self-text", subject.output.postText.value)
    }

    @Test
    fun test_openExternal_openUrl() = runBlockingTest {
        val subject = makeSubject(makeRedditPost(url = "https://test.url.com/"))
        val observer: (t: Event<Uri>?) -> Unit = {}
        subject.output.openExternal.observeForever(observer)

        val mockUri = mockk<Uri>()
        every { Uri.parse("https://test.url.com/") } returns mockUri

        subject.input.onClickOpenExternal.send(Unit)

        assertEquals(mockUri, subject.output.openExternal.value?.getContentIfNotHandled())

        subject.output.openExternal.removeObserver(observer)
    }

    @Test
    fun test_openExternal_openOnReddit() = runBlockingTest {
        val subject = makeSubject(makeRedditPost(permalink = "/r/aaa/1234"))
        val observer: (t: Event<Uri>?) -> Unit = {}
        subject.output.openExternal.observeForever(observer)

        val mockUri = mockk<Uri>()
        every { Uri.parse("https://reddit.com/r/aaa/1234") } returns mockUri

        subject.input.onClickOpenReddit.send(Unit)

        assertEquals(mockUri, subject.output.openExternal.value?.getContentIfNotHandled())

        subject.output.openExternal.removeObserver(observer)
    }

    @Test
    fun test_saveImageToGallery() = runBlockingTest {
        val subject = makeSubject(makeRedditPost(name = "aaa_bbb"))
        val observer: (t: Event<String>?) -> Unit = {}
        subject.output.saveImageToGallery.observeForever(observer)

        subject.input.onClickSaveImage.send(Unit)

        assertEquals(
            "reddit-aaa_bbb",
            subject.output.saveImageToGallery.value?.getContentIfNotHandled()
        )

        subject.output.saveImageToGallery.removeObserver(observer)
    }

    @Test
    fun test_isSaveImageButtonHidden_falseOnImageSuccess() = runBlockingTest {
        val subject = makeSubject()
        val observer: (t: Boolean?) -> Unit = {}
        subject.output.isSaveImageButtonHidden.observeForever(observer)

        // Before image loads, starts with true (hidden):
        assertEquals(true, subject.output.isSaveImageButtonHidden.value)

        subject.input.onImageLoadedSuccessfully.send(Unit)

        // After image loads, button is shown:
        assertEquals(false, subject.output.isSaveImageButtonHidden.value)

        subject.output.isSaveImageButtonHidden.removeObserver(observer)
    }

    @Test
    fun test_isSaveImageButtonHidden_trueOnImageFailure() = runBlockingTest {
        val subject = makeSubject()
        val observer: (t: Boolean?) -> Unit = {}
        subject.output.isSaveImageButtonHidden.observeForever(observer)

        // Before image loads, starts with true (hidden):
        assertEquals(true, subject.output.isSaveImageButtonHidden.value)

        subject.input.onErrorLoadingImage.send(Unit)

        // After image loads, button is shown:
        assertEquals(true, subject.output.isSaveImageButtonHidden.value)

        subject.output.isSaveImageButtonHidden.removeObserver(observer)
    }

    @Test
    fun test_isProgressBarHidden_trueOnImageSuccess() = runBlockingTest {
        val subject = makeSubject()
        val observer: (t: Boolean?) -> Unit = {}
        subject.output.isProgressBarHidden.observeForever(observer)

        // Before image loads, no value was sent:
        assertEquals(null, subject.output.isProgressBarHidden.value)

        subject.input.onImageLoadedSuccessfully.send(Unit)

        // After image loads, progress is hidden:
        assertEquals(true, subject.output.isProgressBarHidden.value)

        subject.output.isProgressBarHidden.removeObserver(observer)
    }

    @Test
    fun test_isProgressBarHidden_trueOnImageFailure() = runBlockingTest {
        val subject = makeSubject()
        val observer: (t: Boolean?) -> Unit = {}
        subject.output.isProgressBarHidden.observeForever(observer)

        // Before image loads, no value was sent:
        assertEquals(null, subject.output.isProgressBarHidden.value)

        subject.input.onErrorLoadingImage.send(Unit)

        // After image fails, progress is hidden:
        assertEquals(true, subject.output.isProgressBarHidden.value)

        subject.output.isProgressBarHidden.removeObserver(observer)
    }

    private fun makeSubject(
        post: RedditPost = makeRedditPost()
    ): PostDetailViewModel {
        return PostDetailViewModel(post)
    }
}