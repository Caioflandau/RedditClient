package com.caiolandau.devigetredditclient.redditpostlist.view

import android.view.View
import android.widget.Button
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostlist.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity
import com.caiolandau.devigetredditclient.util.SnackbarHelper
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class PostListActivityUnitTests {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var mockSnackbarHelper: SnackbarHelper

    @MockK
    private lateinit var mockActivity: IViewModelActivity<PostListViewModel>

    @MockK
    private lateinit var mockViewModel: PostListViewModel

    @MockK
    private lateinit var mockAdapter: PostRecyclerViewAdapter

    @MockK
    lateinit var recyclerViewPosts: RecyclerView

    @MockK
    lateinit var swiperefresh: SwipeRefreshLayout

    @MockK
    private lateinit var btnClearAll: ExtendedFloatingActionButton

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { mockActivity.getViewModel() } returns mockViewModel

        every { mockActivity.findViewById<RecyclerView>(R.id.recyclerViewPosts) } returns recyclerViewPosts
        every { mockActivity.findViewById<SwipeRefreshLayout>(R.id.swiperefresh) } returns swiperefresh
        every { mockActivity.findViewById<Button>(R.id.btnClearAll) } returns btnClearAll

        every { recyclerViewPosts.adapter } returns mockAdapter
    }

    @Test
    fun test_onCreate_input_onClickPostListItem() = runBlockingTest {
        val subject = makeSubject()
        subject.onCreate(null)

        val onItemClickListenerSlot = slot<(RedditPost) -> Unit>()
        verify { mockAdapter.onItemClickListener = capture(onItemClickListenerSlot) }

        val mockBroadcastChannel = mockk<BroadcastChannel<RedditPost>>(relaxed = true)
        every { mockViewModel.input.onClickPostListItem } returns mockBroadcastChannel

        val mockRedditPost = mockk<RedditPost>()
        onItemClickListenerSlot.captured(mockRedditPost)

        verify { mockBroadcastChannel.sendBlocking(mockRedditPost) }
    }

    @Test
    fun test_onCreate_input_onClickDismissPost() = runBlockingTest {
        val subject = makeSubject()
        subject.onCreate(null)

        val onDismissListenerSlot = slot<(RedditPost) -> Unit>()
        verify { mockAdapter.onDismissListener = capture(onDismissListenerSlot) }

        val mockBroadcastChannel = mockk<BroadcastChannel<RedditPost>>(relaxed = true)
        every { mockViewModel.input.onClickDismissPost } returns mockBroadcastChannel

        val mockRedditPost = mockk<RedditPost>()
        onDismissListenerSlot.captured(mockRedditPost)

        verify { mockBroadcastChannel.sendBlocking(mockRedditPost) }
    }

    @Test
    fun test_onCreate_input_onRefresh() = runBlockingTest {
        val subject = makeSubject()
        subject.onCreate(null)

        val onRefreshListenerSlot = slot<SwipeRefreshLayout.OnRefreshListener>()
        verify { swiperefresh.setOnRefreshListener(capture(onRefreshListenerSlot)) }

        val mockBroadcastChannel = mockk<BroadcastChannel<Unit>>(relaxed = true)
        every { mockViewModel.input.onRefresh } returns mockBroadcastChannel

        onRefreshListenerSlot.captured.onRefresh()

        verify { mockBroadcastChannel.sendBlocking(Unit) }
    }

    @Test
    fun test_onCreate_input_onClickDismissAll() = runBlockingTest {
        val subject = makeSubject()
        subject.onCreate(null)

        val onRefreshListenerSlot = slot<View.OnClickListener>()
        verify { btnClearAll.setOnClickListener(capture(onRefreshListenerSlot)) }

        val mockBroadcastChannel = mockk<BroadcastChannel<Unit>>(relaxed = true)
        every { mockViewModel.input.onClickDismissAll } returns mockBroadcastChannel

        onRefreshListenerSlot.captured.onClick(btnClearAll)

        verify { mockBroadcastChannel.sendBlocking(Unit) }
    }

    private fun makeSubject() = PostListActivityWrapper(
        activity = mockActivity,
        snackbarHelper = mockSnackbarHelper
    )
}