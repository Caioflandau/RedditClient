package com.caiolandau.devigetredditclient.redditpostlist.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import com.caiolandau.devigetredditclient.domain.datasource.PagedRedditPostsDataSource
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.repository.RedditPostRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
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

        assertEquals(subject.output.listOfPosts.value, mockPagedList)

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

    private fun makeSubject() = PostListViewModel(
        PostListViewModel.Dependency(mockRedditPostRepository),
        makePagedListLiveData = { _, _ -> listOfPostsLiveData }
    )
}