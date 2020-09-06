package com.caiolandau.devigetredditclient.redditpostlist.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.caiolandau.devigetredditclient.domain.datasource.PagedRedditPostsDataSource
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.repository.RedditPostRepository
import com.caiolandau.devigetredditclient.util.Event
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi // Coroutines / Flow are still marked as experimental, although they are considered stable enough
class PostListViewModelUnitTests {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var listOfPostsLiveData: MutableLiveData<PagedList<RedditPost>>

    @MockK
    private lateinit var mockRedditPostRepository: RedditPostRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(testCoroutineDispatcher)

        listOfPostsLiveData = MutableLiveData<PagedList<RedditPost>>()
    }

    @Test
    fun test_listOfPosts_initialValue() = runBlockingTest {
        val subject = makeSubject()
        val observer: (PagedList<RedditPost>) -> Unit = {}
        subject.output.listOfPosts
            .observeForever(observer)

        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        listOfPostsLiveData.postValue(mockPagedList)

        assertEquals(mockPagedList, subject.output.listOfPosts.value)

        subject.output.listOfPosts.removeObserver(observer)
    }

    @Test
    fun test_listOfPosts_onClickDismissPost() = runBlockingTest {
        val subject = makeSubject()
        val observer: (PagedList<RedditPost>) -> Unit = {}
        subject.output.listOfPosts
            .observeForever(observer)

        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        listOfPostsLiveData.postValue(mockPagedList)

        val mockDataSource = mockk<DataSource<String, RedditPost>>(relaxed = true)
        every { mockPagedList.dataSource } returns mockDataSource

        val mockPost = mockk<RedditPost>()
        subject.input.onClickDismissPost.send(mockPost)

        verify { mockRedditPostRepository.filterPost(mockPost) }
        verify { mockDataSource.invalidate() }

        subject.output.listOfPosts.removeObserver(observer)
    }

    @Test
    fun test_listOfPosts_onClickPostListItem() = runBlockingTest {
        val subject = makeSubject()
        val observer: (PagedList<RedditPost>) -> Unit = {}
        subject.output.listOfPosts
            .observeForever(observer)

        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        listOfPostsLiveData.postValue(mockPagedList)

        val mockDataSource = mockk<DataSource<String, RedditPost>>(relaxed = true)
        every { mockPagedList.dataSource } returns mockDataSource

        val mockPost = mockk<RedditPost>()
        subject.input.onClickPostListItem.send(mockPost)

        verify { mockRedditPostRepository.markPostAsRead(mockPost) }
        verify { mockDataSource.invalidate() }

        subject.output.listOfPosts.removeObserver(observer)
    }

    @Test
    fun test_showPostDetails() = runBlockingTest {
        val subject = makeSubject()

        val observer: (Event<RedditPost>) -> Unit = {}
        subject.output.showPostDetails
            .observeForever(observer)

        val mockPost = mockk<RedditPost>()
        subject.input.onClickPostListItem.send(mockPost)

        assertEquals(mockPost, subject.output.showPostDetails.value?.peekContent())

        subject.output.showPostDetails.removeObserver(observer)
    }

    @Test
    fun test_closePostDetails_dismissedCurrentPost() = runBlockingTest {
        // Tests the case where we are dismissing the currently displaying post:
        val subject = makeSubject()

        val observerClosePostDetails: (Event<Unit>) -> Unit = {}
        subject.output.closePostDetails
            .observeForever(observerClosePostDetails)

        // Display a post:
        val mockPost = mockk<RedditPost>()
        every { mockPost.id } returns "aaa_123"
        subject.input.onClickPostListItem.send(mockPost)

        // Assert that no value was emitted before dismissing:
        assertEquals(null, subject.output.closePostDetails.value)

        // Dismiss the same post:
        subject.input.onClickDismissPost.send(mockPost)

        // Assert a value was emitted after dismissing:
        assertNotNull(subject.output.closePostDetails.value)

        subject.output.closePostDetails.removeObserver(observerClosePostDetails)
    }

    @Test
    fun test_closePostDetails_clearAll() = runBlockingTest {
        // Tests the case where we are dismissing all posts - should clear the current details:
        val subject = makeSubject()

        val observerClosePostDetails: (Event<Unit>) -> Unit = {}
        subject.output.closePostDetails
            .observeForever(observerClosePostDetails)

        // Display a post:
        val mockPost = mockk<RedditPost>()
        every { mockPost.id } returns "aaa_123"
        subject.input.onClickPostListItem.send(mockPost)

        // Assert that no value was emitted before dismissing:
        assertEquals(null, subject.output.closePostDetails.value)

        // Dismiss all posts:
        subject.input.onClickDismissAll.send(Unit)

        // Assert a value was emitted after dismissing:
        assertNotNull(subject.output.closePostDetails.value)

        subject.output.closePostDetails.removeObserver(observerClosePostDetails)
    }

    @Test
    fun test_errorLoadingPage() {
        mockkObject(PagedRedditPostsDataSource.Companion)
        every { PagedRedditPostsDataSource.getFactory(any(), any(), any()) } returns mockk()

        val subject = makeSubject()

        val observer: (Event<Unit>) -> Unit = {}
        subject.output.errorLoadingPage
            .observeForever(observer)

        // Assert that no value was emitted before an error callback:
        assertEquals(null, subject.output.errorLoadingPage.value)

        // Call the error callback:
        val errorCallbackSlot = slot<() -> Unit>()
        verify { PagedRedditPostsDataSource.getFactory(any(), any(), capture(errorCallbackSlot)) }
        errorCallbackSlot.captured()

        // Assert a value was emitted after the error callback:
        assertNotNull(subject.output.errorLoadingPage.value)

        subject.output.errorLoadingPage.removeObserver(observer)
    }

    @Test
    fun test_isRefreshing() = runBlockingTest {
        // Tests the following sequence of events:
        // 1. ViewModel starts with isRefreshing = true
        // 2. Emits listOfPosts (isRefreshing still true)
        // 3. Calls PagedList.Callback (isRefreshing = false)
        // 4. Emits onRefresh (isRefreshing = true)
        val subject = makeSubject()

        val observer: (Boolean) -> Unit = {}
        subject.output.isRefreshing
            .observeForever(observer)

        // Assert that ViewModel starts with isRefreshing = true:
        assertEquals(true, subject.output.isRefreshing.value)

        // Emits listOfPosts:
        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        listOfPostsLiveData.postValue(mockPagedList)

        // Assert that isRefreshing is still true:
        assertEquals(true, subject.output.isRefreshing.value)

        // Call PagedList.Callback:
        val onLoadCallbackSlot = slot<PagedList.Callback>()
        verify { mockPagedList.addWeakCallback(null, capture(onLoadCallbackSlot)) }
        onLoadCallbackSlot.captured.onInserted(0, 10)

        // Assert that isRefreshing is now false:
        assertEquals(false, subject.output.isRefreshing.value)

        // Emits onRefresh:
        subject.input.onRefresh.send(Unit)

        // Assert that a true value was emitted:
        assertEquals(true, subject.output.isRefreshing.value)

        subject.output.isRefreshing.removeObserver(observer)
    }

    @Test
    fun test_clearedAll() = runBlockingTest {
        val subject = makeSubject()

        val observer: (Event<Unit>) -> Unit = {}
        subject.output.clearedAll
            .observeForever(observer)

        // Assert that no value was emitted before the input:
        assertEquals(null, subject.output.clearedAll.value)

        subject.input.onClickDismissAll.send(Unit)

        // Assert that a value was emitted:
        assertNotNull(subject.output.clearedAll.value)

        subject.output.clearedAll.removeObserver(observer)
    }

    private fun makeSubject() = PostListViewModel(
        mockRedditPostRepository,
        makePagedListLiveData = { _, _ -> listOfPostsLiveData }
    )
}