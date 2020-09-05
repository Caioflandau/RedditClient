package com.caiolandau.devigetredditclient.redditpostlist.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedList
import coil.ImageLoader
import coil.request.ImageRequest
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.test_utils.makeRedditPost
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PostRecyclerViewAdapterUnitTests {

    @MockK
    private lateinit var mockOnClickListener: (RedditPost) -> Unit

    @MockK
    private lateinit var mockOnDismissListener: (RedditPost) -> Unit

    @MockK
    private lateinit var mockImageLoader: ImageLoader

    @MockK
    private lateinit var mockContentView: View

    @MockK
    private lateinit var txtPosterName: TextView

    @MockK
    private lateinit var txtPostTime: TextView

    @MockK
    private lateinit var txtPostTitle: TextView

    @MockK
    private lateinit var txtPostCommentCount: TextView

    @MockK
    private lateinit var imgPostThumbnail: ImageView

    @MockK
    private lateinit var btnDismissPost: Button

    @MockK
    private lateinit var frmUnreadIndicator: FrameLayout


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockContentView.findViewById<TextView>(R.id.txtPosterName) } returns txtPosterName
        every { mockContentView.findViewById<TextView>(R.id.txtPostTime) } returns txtPostTime
        every { mockContentView.findViewById<TextView>(R.id.txtPostTitle) } returns txtPostTitle
        every { mockContentView.findViewById<TextView>(R.id.txtPostCommentCount) } returns txtPostCommentCount
        every { mockContentView.findViewById<ImageView>(R.id.imgPostThumbnail) } returns imgPostThumbnail
        every { mockContentView.findViewById<Button>(R.id.btnDismissPost) } returns btnDismissPost
        every { mockContentView.findViewById<FrameLayout>(R.id.frmUnreadIndicator) } returns frmUnreadIndicator
    }

    @Test
    fun test_onCreateViewHolder() {
        val subject = makeSubject()

        val parentView = mockk<ViewGroup>()
        val mockContext = mockk<Context>()
        every { parentView.context } returns mockContext

        // Mock LayoutInflater:
        val mockLayoutInflater = mockk<LayoutInflater>()
        mockkStatic(LayoutInflater::class)
        every { LayoutInflater.from(mockContext) } returns mockLayoutInflater
        every {
            mockLayoutInflater.inflate(R.layout.post_list_content, parentView, false)
        } returns mockContentView

        val viewHolder = subject.onCreateViewHolder(parentView, 0)
        assertSame(txtPosterName, viewHolder.txtPosterName)
        assertSame(txtPostTime, viewHolder.txtPostTime)
        assertSame(txtPostTitle, viewHolder.txtPostTitle)
        assertSame(txtPostCommentCount, viewHolder.txtPostCommentCount)
        assertSame(imgPostThumbnail, viewHolder.imgPostThumbnail)
        assertSame(btnDismissPost, viewHolder.btnDismissPost)
        assertSame(frmUnreadIndicator, viewHolder.frmUnreadIndicator)
    }

    @Test
    fun test_bindViewHolder() {
        val subject = makeSubject()
        val parentView = mockk<ViewGroup>()
        val mockContext = mockk<Context>()
        every { parentView.context } returns mockContext
        every { mockContentView.context } returns mockContext

        // Mock LayoutInflater:
        val mockLayoutInflater = mockk<LayoutInflater>()
        mockkStatic(LayoutInflater::class)
        every { LayoutInflater.from(mockContext) } returns mockLayoutInflater
        every {
            mockLayoutInflater.inflate(R.layout.post_list_content, parentView, false)
        } returns mockContentView

        // Mock context.getString(..):
        every { mockContext.getString(R.string.num_of_comments, any()) } returns "123 comments"

        // Create a viewHolder to test with:
        val viewHolder = subject.onCreateViewHolder(parentView, 0)

        // Call the method under test:
        val post = makeRedditPost(isRead = true)
        subject.bindViewHolder(post, viewHolder)

        // Start assertions:
        verify { txtPosterName.text = post.author }
        verify { txtPostTime.text = post.entryDate }
        verify { txtPostTitle.text = post.title }
        verify { txtPostCommentCount.text = "123 comments" }
        verify { frmUnreadIndicator.visibility = View.GONE }

        val imageRequestSlot = slot<ImageRequest>()
        verify { mockImageLoader.enqueue(capture(imageRequestSlot)) }
        assertEquals(post.thumbnailUrl, imageRequestSlot.captured.data)

        val onItemClickListenerSlot = slot<View.OnClickListener>()
        verify {
            mockContentView.setOnClickListener(capture(onItemClickListenerSlot))
        }
        onItemClickListenerSlot.captured.onClick(mockContentView)
        verify { mockOnClickListener.invoke(post) }

        val onDismissClickListenerSlot = slot<View.OnClickListener>()
        verify {
            btnDismissPost.setOnClickListener(capture(onDismissClickListenerSlot))
        }
        onDismissClickListenerSlot.captured.onClick(btnDismissPost)
        verify {
            mockOnDismissListener.invoke(post)
            frmUnreadIndicator.visibility = View.GONE
        }
    }

    private fun makeSubject() = PostRecyclerViewAdapter(mockImageLoader).apply {
        onItemClickListener = mockOnClickListener
        onDismissListener = mockOnDismissListener
    }
}