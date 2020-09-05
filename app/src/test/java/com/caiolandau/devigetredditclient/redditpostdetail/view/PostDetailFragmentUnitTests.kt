package com.caiolandau.devigetredditclient.redditpostdetail.view

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import coil.ImageLoader
import coil.request.ImageRequest
import com.caiolandau.devigetredditclient.R
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.redditpostdetail.viewmodel.PostDetailViewModel
import com.caiolandau.devigetredditclient.util.Event
import com.caiolandau.devigetredditclient.util.IFragment
import com.caiolandau.devigetredditclient.util.LocalImageSaver
import com.caiolandau.devigetredditclient.util.SnackbarHelper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@FlowPreview @ExperimentalCoroutinesApi
class PostDetailFragmentUnitTests {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var lifecycle: LifecycleRegistry

    @MockK(relaxed = true)
    private lateinit var mockFragment: IFragment<RedditPost, PostDetailViewModel>

    @MockK
    private lateinit var mockInflater: LayoutInflater

    @MockK(relaxed = true)
    private lateinit var mockLocalImageSaver: LocalImageSaver

    @MockK(relaxed = true)
    private lateinit var mockSnackbarHelper: SnackbarHelper

    @MockK(relaxed = true)
    private lateinit var mockRootView: View

    @MockK(relaxed = true)
    private lateinit var mockImageLoader: ImageLoader

    // Mock views:
    @MockK(relaxed = true)
    private lateinit var txtPostTitle: TextView

    @MockK(relaxed = true)
    private lateinit var txtPostText: TextView

    @MockK(relaxed = true)
    private lateinit var imgPostImage: ImageView

    @MockK(relaxed = true)
    private lateinit var btnSaveImage: Button

    @MockK(relaxed = true)
    private lateinit var progressLoadingImage: ProgressBar

    @MockK(relaxed = true)
    private lateinit var btnOpenExternal: Button

    @MockK(relaxed = true)
    private lateinit var btnOpenReddit: Button

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockRootView.findViewById<TextView>(R.id.txtPostTitle) } returns txtPostTitle
        every { mockRootView.findViewById<TextView>(R.id.txtPostText) } returns txtPostText
        every { mockRootView.findViewById<ImageView>(R.id.imgPostImage) } returns imgPostImage
        every { mockRootView.findViewById<Button>(R.id.btnSaveImage) } returns btnSaveImage
        every { mockRootView.findViewById<ProgressBar>(R.id.progressLoadingImage) } returns progressLoadingImage
        every { mockRootView.findViewById<Button>(R.id.btnOpenExternal) } returns btnOpenExternal
        every { mockRootView.findViewById<Button>(R.id.btnOpenReddit) } returns btnOpenReddit
    }

    @Test
    fun test_onCreateView_output() {
        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        val imageRequestSlot = slot<ImageRequest>()
        verify {
            txtPostTitle.text = output.postTitle.value
            txtPostText.text = output.postText.value
            mockImageLoader.enqueue(capture(imageRequestSlot))
        }

        assertEquals(output.postImageUrl.value, imageRequestSlot.captured.data)
    }

    @Test
    fun test_onCreateView_output_openExternal() {
        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        val mockContext = mockk<Context>(relaxed = true)
        every { mockFragment.ctx } returns mockContext

        val openExternal = output.openExternal as MutableLiveData<Event<Uri>>
        openExternal.postValue(Event(mockk()))

        verify {
            mockContext.startActivity(any<Intent>())
        }
    }

    @Test
    fun test_onCreateView_output_isSaveImageButtonHidden_true() {
        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        val isSaveImageButtonHidden = output.isSaveImageButtonHidden as MutableLiveData<Boolean>
        isSaveImageButtonHidden.postValue(true)

        verify {
            btnSaveImage.visibility = View.GONE
        }
    }

    @Test
    fun test_onCreateView_output_isSaveImageButtonHidden_false() {
        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        val isSaveImageButtonHidden = output.isSaveImageButtonHidden as MutableLiveData<Boolean>
        isSaveImageButtonHidden.postValue(false)

        verify {
            btnSaveImage.visibility = View.VISIBLE
        }
    }

    @Test
    fun test_onCreateView_output_isProgressBarHidden_true() {
        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        val isProgressBarHidden = output.isProgressBarHidden as MutableLiveData<Boolean>
        isProgressBarHidden.postValue(true)

        verify {
            progressLoadingImage.visibility = View.GONE
        }
    }

    @Test
    fun test_onCreateView_output_isProgressBarHidden_false() {
        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        val isProgressBarHidden = output.isProgressBarHidden as MutableLiveData<Boolean>
        isProgressBarHidden.postValue(false)

        verify {
            progressLoadingImage.visibility = View.VISIBLE
        }
    }

    @Test
    fun test_onCreateView_output_saveImageToGallery() {
        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        val mockDrawable = mockk<Drawable>()
        every { imgPostImage.drawable } returns mockDrawable

        val mockContext = mockk<Context>(relaxed = true)
        every { mockFragment.ctx } returns mockContext

        val saveImageToGallery = output.saveImageToGallery as MutableLiveData<Event<String>>
        saveImageToGallery.postValue(Event("image-file.jpg"))

        verify {
            mockLocalImageSaver.saveImageToGallery(mockContext, "image-file.jpg", mockDrawable)
            mockSnackbarHelper.showSnackbar(mockRootView, R.string.image_saved_success_message)
        }
    }

    @Test
    fun test_onCreateView_input_onClickOpenExternal() = runBlockingTest {
        val onClickListenerSlot = slot<View.OnClickListener>()
        every { btnOpenExternal.setOnClickListener(capture(onClickListenerSlot)) } returns Unit

        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        var triggered = false
        vm.input.onClickOpenExternal.asFlow()
            .onEach {
                triggered = true
                closeChannels(vm)
            }
            .launchIn(this)

        onClickListenerSlot.captured.onClick(mockk())

        assertTrue(triggered)
        cleanupTestCoroutines()
    }

    @Test
    fun test_onCreateView_input_onClickOpenReddit() = runBlockingTest {
        val onClickListenerSlot = slot<View.OnClickListener>()
        every { btnOpenReddit.setOnClickListener(capture(onClickListenerSlot)) } returns Unit

        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        var triggered = false
        vm.input.onClickOpenReddit.asFlow()
            .onEach {
                triggered = true
                closeChannels(vm)
            }
            .launchIn(this)

        onClickListenerSlot.captured.onClick(mockk())

        assertTrue(triggered)
        cleanupTestCoroutines()
    }

    @Test
    fun test_onCreateView_input_onClickSaveImage() = runBlockingTest {
        val onClickListenerSlot = slot<View.OnClickListener>()
        every { btnSaveImage.setOnClickListener(capture(onClickListenerSlot)) } returns Unit

        val output = mockOutput()
        val vm = makeViewModel(output)
        makeSubject(vm)

        var triggered = false
        vm.input.onClickSaveImage.asFlow()
            .onEach {
                triggered = true
                closeChannels(vm)
            }
            .launchIn(this)

        onClickListenerSlot.captured.onClick(mockk())

        assertTrue(triggered)
        cleanupTestCoroutines()
    }

    private fun makeViewModel(mockOutput: PostDetailViewModel.Output): PostDetailViewModel {
        return mockk<PostDetailViewModel>().apply {
            every { output } returns mockOutput
            every { input } returns PostDetailViewModel.Input()
        }
    }

    private fun mockOutput() = PostDetailViewModel.Output(
        postTitle = MutableLiveData("The title"),
        postImageUrl = MutableLiveData("https://the-url.com/img.png"),
        postText = MutableLiveData("The self-text"),
        openExternal = MutableLiveData(),
        isSaveImageButtonHidden = MutableLiveData(false),
        saveImageToGallery = MutableLiveData(),
        isProgressBarHidden = MutableLiveData(false)
    )

    private fun closeChannels(vm: PostDetailViewModel) {
        vm.input.onClickOpenExternal.close()
        vm.input.onClickOpenReddit.close()
        vm.input.onImageLoadedSuccessfully.close()
        vm.input.onClickSaveImage.close()
        vm.input.onErrorLoadingImage.close()
    }

    private fun makeSubject(vm: PostDetailViewModel): PostDetailFragmentWrapper {
        val subject =
            PostDetailFragmentWrapper(
                mockFragment,
                mockImageLoader,
                mockLocalImageSaver,
                mockSnackbarHelper
            ).apply { viewModel = vm }

        // Mocks the lifecycle owner for the fragment:
        val lifecycleOwner: LifecycleOwner = mockk()
        lifecycle = LifecycleRegistry(lifecycleOwner)
        every { lifecycleOwner.lifecycle } returns lifecycle
        every { mockFragment.viewLifecycleOwner } returns lifecycleOwner

        val mockContainer = mockk<ViewGroup>()

        every {
            mockInflater.inflate(R.layout.post_detail, mockContainer, false)
        } returns mockRootView

        subject.onCreateView(mockInflater, mockContainer, null)

        // Simulates "onResume" in the lifecycle owner (triggers LiveData to start emitting):
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        return subject
    }
}