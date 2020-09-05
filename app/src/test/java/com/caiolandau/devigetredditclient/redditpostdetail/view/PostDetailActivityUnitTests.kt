package com.caiolandau.devigetredditclient.redditpostdetail.view

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.caiolandau.devigetredditclient.util.IActivity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostDetailActivityUnitTests {

    @MockK(relaxed = true)
    private lateinit var mockActivity: IActivity

    @MockK(relaxed = true)
    private lateinit var mockFragment: PostDetailFragment

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test_onCreate() {
        val subject = createSubject()

        val mockFragmentTransaction = mockk<FragmentTransaction>(relaxed = true)
        every {
            mockActivity.getSupportFragmentManager().beginTransaction()
        } returns mockFragmentTransaction
        every { mockFragmentTransaction.add(any(), mockFragment) } returns mockFragmentTransaction
        every { mockFragmentTransaction.commit() } returns 1

        subject.onCreate(null)

        verifyOrder {
            mockFragmentTransaction.add(any(), mockFragment)
            mockFragmentTransaction.commit()
        }
    }

    @Test
    fun test_onOptionsItemSelected() {
        val subject = createSubject()
        val mockMenuItem = mockk<MenuItem>()
        every { mockMenuItem.itemId } returns android.R.id.home

        val returnValue = subject.onOptionsItemSelected(mockMenuItem)

        verify { mockActivity.navigateUpTo(any()) }
        assertTrue(returnValue)
    }

    private fun createSubject() =
        PostDetailActivityWrapper(mockActivity, makePostDetailFragment = { mockFragment })
}