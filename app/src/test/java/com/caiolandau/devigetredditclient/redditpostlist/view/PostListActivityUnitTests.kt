package com.caiolandau.devigetredditclient.redditpostlist.view

import android.widget.Button
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.redditpostlist.viewmodel.PostListViewModel
import com.caiolandau.devigetredditclient.util.IViewModelActivity
import com.caiolandau.devigetredditclient.util.SnackbarHelper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule

@FlowPreview
@ExperimentalCoroutinesApi
class PostListActivityUnitTests {

    @MockK
    private lateinit var mockSnackbarHelper: SnackbarHelper

    @MockK
    private lateinit var mockActivity: IViewModelActivity<PostListViewModel>

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var recyclerViewPosts: RecyclerView

    @MockK
    lateinit var swiperefresh: SwipeRefreshLayout

    @MockK
    private lateinit var btnClearAll: Button

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { mockActivity.findViewById<RecyclerView>(R.id.recyclerViewPosts) } returns recyclerViewPosts
        every { mockActivity.findViewById<SwipeRefreshLayout>(R.id.swiperefresh) } returns swiperefresh
        every { mockActivity.findViewById<Button>(R.id.btnClearAll) } returns btnClearAll
    }

    fun makeSubject() = PostListActivityWrapper(
        activity = mockActivity,
        snackbarHelper = mockSnackbarHelper
    )
}