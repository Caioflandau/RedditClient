package com.caiolandau.devigetredditclient.redditpostlist.view

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostdetail.view.PostDetailActivity
import com.caiolandau.devigetredditclient.redditpostdetail.view.PostDetailFragment
import com.caiolandau.devigetredditclient.redditpostlist.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity
import com.caiolandau.devigetredditclient.util.SnackbarHelper
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class PostListActivityUnitTests {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var lifecycle: LifecycleRegistry

    @MockK
    private lateinit var mockSnackbarHelper: SnackbarHelper

    @MockK
    private lateinit var mockActivity: IViewModelActivity<PostListViewModel>

    @MockK
    private lateinit var mockViewModel: PostListViewModel

    @MockK
    private lateinit var mockPostDetailFragment: PostDetailFragment

    @MockK
    private lateinit var mockAdapter: PostRecyclerViewAdapter

    // Mock views:
    @MockK
    lateinit var recyclerViewPosts: RecyclerView

    @MockK
    lateinit var swiperefresh: SwipeRefreshLayout

    @MockK
    private lateinit var btnClearAll: ExtendedFloatingActionButton

    @MockK
    private lateinit var frmPostDetailContainer: NestedScrollView

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(testCoroutineDispatcher)

        every { mockActivity.getViewModel() } returns mockViewModel

        every { mockActivity.findViewById<RecyclerView>(R.id.recyclerViewPosts) } returns recyclerViewPosts
        every { mockActivity.findViewById<SwipeRefreshLayout>(R.id.swiperefresh) } returns swiperefresh
        every { mockActivity.findViewById<Button>(R.id.btnClearAll) } returns btnClearAll

        every { recyclerViewPosts.adapter } returns mockAdapter
    }

    @Test
    fun test_onCreate_input_onClickPostListItem() = runBlockingTest {
        val subject = makeSubject()
        setupSubject(subject)

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
        setupSubject(subject)

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
        setupSubject(subject)

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
        setupSubject(subject)

        val onRefreshListenerSlot = slot<View.OnClickListener>()
        verify { btnClearAll.setOnClickListener(capture(onRefreshListenerSlot)) }

        val mockBroadcastChannel = mockk<BroadcastChannel<Unit>>(relaxed = true)
        every { mockViewModel.input.onClickDismissAll } returns mockBroadcastChannel

        onRefreshListenerSlot.captured.onClick(btnClearAll)

        verify { mockBroadcastChannel.sendBlocking(Unit) }
    }

    @Test
    fun test_onCreate_output_listOfPosts_nonEmpty() = runBlockingTest {
        // Tests that when a non-empty PagedList is emitted, we simply submit it right away
        val listOfPostsOutput = MutableLiveData<PagedList<RedditPost>>()
        every { mockViewModel.output.listOfPosts } returns listOfPostsOutput
        val observer: (PagedList<RedditPost>) -> Unit = {}

        val subject = makeSubject()
        listOfPostsOutput.observeForever(observer)
        setupSubject(subject)

        val mockPagedList = mockk<PagedList<RedditPost>>()
        every { mockPagedList.size } returns 10 // Non-empty list
        listOfPostsOutput.postValue(mockPagedList)

        verify(exactly = 1) { mockAdapter.submitList(mockPagedList) }

        listOfPostsOutput.removeObserver(observer)
    }

    @Test
    fun test_onCreate_output_listOfPosts_empty_submitsOnChange() = runBlockingTest {
        // Tests that when an empty PagedList is emitted, we add a callback and submit the list only
        // when the onChanged callback is called
        val listOfPostsOutput = MutableLiveData<PagedList<RedditPost>>()
        every { mockViewModel.output.listOfPosts } returns listOfPostsOutput
        val observer: (PagedList<RedditPost>) -> Unit = {}

        val subject = makeSubject()
        listOfPostsOutput.observeForever(observer)
        setupSubject(subject)

        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        every { mockPagedList.size } returns 0 // Empty list
        listOfPostsOutput.postValue(mockPagedList)

        // No list should have been submited, since it was empty:
        verify(exactly = 0) { mockAdapter.submitList(mockPagedList) }

        // A callback should have been added instead of submitting the list:
        val addWeakCallbackSlot = slot<PagedList.Callback>()
        verify { mockPagedList.addWeakCallback(null, capture(addWeakCallbackSlot)) }
        addWeakCallbackSlot.captured.onChanged(0, 1)

        // After calling the callback, the list should submitted once:
        verify(exactly = 1) { mockAdapter.submitList(mockPagedList) }

        listOfPostsOutput.removeObserver(observer)
    }

    @Test
    fun test_onCreate_output_listOfPosts_empty_submitsOnInserted() = runBlockingTest {
        // Tests that when an empty PagedList is emitted, we add a callback and submit the list only
        // when the onInserted callback is called
        val listOfPostsOutput = MutableLiveData<PagedList<RedditPost>>()
        every { mockViewModel.output.listOfPosts } returns listOfPostsOutput
        val observer: (PagedList<RedditPost>) -> Unit = {}

        val subject = makeSubject()
        listOfPostsOutput.observeForever(observer)
        setupSubject(subject)

        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        every { mockPagedList.size } returns 0 // Empty list
        listOfPostsOutput.postValue(mockPagedList)

        // No list should have been submited, since it was empty:
        verify(exactly = 0) { mockAdapter.submitList(mockPagedList) }

        // A callback should have been added instead of submitting the list:
        val addWeakCallbackSlot = slot<PagedList.Callback>()
        verify { mockPagedList.addWeakCallback(null, capture(addWeakCallbackSlot)) }
        addWeakCallbackSlot.captured.onInserted(0, 1)

        // After calling the callback, the list should submitted once:
        verify(exactly = 1) { mockAdapter.submitList(mockPagedList) }

        listOfPostsOutput.removeObserver(observer)
    }

    @Test
    fun test_onCreate_output_listOfPosts_empty_submitsOnRemoved() = runBlockingTest {
        // Tests that when an empty PagedList is emitted, we add a callback and submit the list only
        // when the onRemoved callback is called
        val listOfPostsOutput = MutableLiveData<PagedList<RedditPost>>()
        every { mockViewModel.output.listOfPosts } returns listOfPostsOutput
        val observer: (PagedList<RedditPost>) -> Unit = {}

        val subject = makeSubject()
        listOfPostsOutput.observeForever(observer)
        setupSubject(subject)

        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        every { mockPagedList.size } returns 0 // Empty list
        listOfPostsOutput.postValue(mockPagedList)

        // No list should have been submited, since it was empty:
        verify(exactly = 0) { mockAdapter.submitList(mockPagedList) }

        // A callback should have been added instead of submitting the list:
        val addWeakCallbackSlot = slot<PagedList.Callback>()
        verify { mockPagedList.addWeakCallback(null, capture(addWeakCallbackSlot)) }
        addWeakCallbackSlot.captured.onRemoved(0, 1)

        // After calling the callback, the list should submitted once:
        verify(exactly = 1) { mockAdapter.submitList(mockPagedList) }

        listOfPostsOutput.removeObserver(observer)
    }

    @Test
    fun test_onCreate_output_showPostDetails_twoPane() = runBlockingTest {
        // Mock two panes (it's two panes when this view is present):
        every {
            mockActivity.findViewById<NestedScrollView>(R.id.frmPostDetailContainer)
        } returns frmPostDetailContainer

        // Mock the FragmentManager:
        val mockFragmentTransaction = mockk<FragmentTransaction>(relaxed = true)
        every {
            mockActivity.getSupportFragmentManager().beginTransaction()
        } returns mockFragmentTransaction
        every {
            mockFragmentTransaction.replace(any(), mockPostDetailFragment)
        } returns mockFragmentTransaction

        //Details will only be shown if the post is still on the list, so mock it:
        val mockRedditPost = mockk<RedditPost>()
        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        val listOfPostsOutput = MutableLiveData(mockPagedList)
        every { mockViewModel.output.listOfPosts } returns listOfPostsOutput
        every { mockPagedList.contains(mockRedditPost) } returns true

        // Mock the output we're testing:
        val showPostDetailsOutput = MutableLiveData<RedditPost>()
        every { mockViewModel.output.showPostDetails } returns showPostDetailsOutput
        val observer: (RedditPost) -> Unit = {}

        val subject = makeSubject()
        showPostDetailsOutput.observeForever(observer)
        setupSubject(subject)

        showPostDetailsOutput.postValue(mockRedditPost)

        verify(exactly = 1) { mockFragmentTransaction.replace(any(), mockPostDetailFragment) }

        showPostDetailsOutput.removeObserver(observer)
    }

    @Test
    fun test_onCreate_output_showPostDetails_singlePane() = runBlockingTest {
        // Mock single (it's single pane when this view is NOT present):
        every {
            mockActivity.findViewById<NestedScrollView>(R.id.frmPostDetailContainer)
        } returns null

        //Details will only be shown if the post is still on the list, so mock it:
        val mockRedditPost = mockk<RedditPost>()
        val mockPagedList = mockk<PagedList<RedditPost>>(relaxed = true)
        val listOfPostsOutput = MutableLiveData(mockPagedList)
        every { mockViewModel.output.listOfPosts } returns listOfPostsOutput
        every { mockPagedList.contains(mockRedditPost) } returns true

        // Mock the context (for startActivity)
        val mockContext = mockk<Context>(relaxed = true)
        every { mockActivity.context } returns mockContext

        // Mock the output we're testing:
        val showPostDetailsOutput = MutableLiveData<RedditPost>()
        every { mockViewModel.output.showPostDetails } returns showPostDetailsOutput
        val observer: (RedditPost) -> Unit = {}

        // Mock PostDetailActivity.newIntent(..):
        val mockIntent = mockk<Intent>()
        mockkObject(PostDetailActivity.Companion)
        every { PostDetailActivity.newIntent(mockContext, mockRedditPost) } returns mockIntent

        val subject = makeSubject()
        showPostDetailsOutput.observeForever(observer)
        setupSubject(subject)

        // Emit the output we're testing:
        showPostDetailsOutput.postValue(mockRedditPost)

        verify { mockContext.startActivity(mockIntent) }

        showPostDetailsOutput.removeObserver(observer)
    }

    // Calls onCreate and emits ON_RESUME lifecycle event to mimic a regular Activity
    private fun setupSubject(subject: PostListActivityWrapper) {
        subject.onCreate(null)

        // Simulates "onResume" in the lifecycle owner (triggers LiveData to start emitting):
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun makeSubject(): PostListActivityWrapper {
        // Mocks the lifecycle owner from the activity:
        val lifecycleOwner: LifecycleOwner = mockk()
        lifecycle = LifecycleRegistry(lifecycleOwner)
        every { lifecycleOwner.lifecycle } returns lifecycle
        every { mockActivity.lifecycleOwner() } returns lifecycleOwner

        // Mock the viewModel
        every { mockActivity.getViewModel() } returns mockViewModel

        val subject = PostListActivityWrapper(
            activity = mockActivity,
            snackbarHelper = mockSnackbarHelper,
            makePostDetailFragment = { mockPostDetailFragment }
        )

        return subject
    }
}