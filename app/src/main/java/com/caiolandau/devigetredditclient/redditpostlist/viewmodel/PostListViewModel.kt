package com.caiolandau.devigetredditclient.redditpostlist.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.caiolandau.devigetredditclient.domain.api.Api
import com.caiolandau.devigetredditclient.domain.datasource.PagedRedditPostsDataSource
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.repository.RedditPostRepository
import com.caiolandau.devigetredditclient.util.Event
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*

class PostListViewModel(
    dependency: Dependency = Dependency()
) : ViewModel() {

    class Dependency(
        val redditPostRepository: RedditPostRepository = RedditPostRepository(Api().reddit)
    )

    /**
     * Represents input events - i.e. list item clicks - that are possible from the view:
     */
    class Input {
        val onClickPostListItem: BroadcastChannel<RedditPost> = BroadcastChannel(1)
        val onClickDismissPost: BroadcastChannel<RedditPost> = BroadcastChannel(1)
        val onClickDismissAll: BroadcastChannel<Unit> = BroadcastChannel(1)
        val onRefresh: BroadcastChannel<Unit> = BroadcastChannel(1)
    }

    /**
     * Represents outputs - i.e. list of posts - to be presented/handled by the view
     */
    class Output(
        val listOfPosts: LiveData<PagedList<RedditPost>>,
        val showPostDetails: LiveData<RedditPost>,
        val closePostDetails: LiveData<Event<Unit>>,
        val errorLoadingPage: LiveData<Event<Unit>>,
        val isRefreshing: LiveData<Boolean>,
        val clearedAll: LiveData<Event<Unit>>
    )

    val input: Input = Input()
    val output: Output = initOutput(dependency.redditPostRepository)

    private fun initOutput(redditPostRepository: RedditPostRepository): Output {
        val errorLoadingPage = MutableLiveData<Event<Unit>>()
        val dataSourceFactory =
            PagedRedditPostsDataSource.getFactory(redditPostRepository, viewModelScope) {
                errorLoadingPage.postValue(Event(Unit))
            }

        val listOfPosts = initListOfPostsOutput(dataSourceFactory, redditPostRepository)
        val showPostDetails = initShowPostDetailsOutput(listOfPosts)
        val isRefreshing =
            initOutputIsRefreshing(redditPostRepository, listOfPosts, errorLoadingPage)
        val clearedAll = initOutputClearedAll()
        val closePostDetails = initClosePostDetails(clearedAll, showPostDetails)
        return Output(
            listOfPosts = listOfPosts,
            showPostDetails = showPostDetails,
            closePostDetails = closePostDetails,
            errorLoadingPage = errorLoadingPage,
            isRefreshing = isRefreshing,
            clearedAll = clearedAll
        )
    }

    private fun initClosePostDetails(
        clearedAll: MutableLiveData<Event<Unit>>,
        showPostDetails: MutableLiveData<RedditPost>
    ) =
        MutableLiveData<Event<Unit>>().apply {
            input.onClickDismissPost
                .asFlow()
                .combine(showPostDetails.asFlow()) { dismissedPost, currentDetailsPost ->
                    Pair(dismissedPost, currentDetailsPost)
                }
                .filter { it.first.id == it.second?.id } // If currently showing the post we're dismissing...
                .onEach {
                    // ...emit a "close post details" event:
                    postValue(Event(Unit))
                }
                .launchIn(viewModelScope)

            // Emits a "close post details" event when clearing all posts:
            clearedAll
                .asFlow()
                .onEach {
                    postValue(Event(Unit))
                }
                .launchIn(viewModelScope)
        }

    private fun initOutputClearedAll() = MutableLiveData<Event<Unit>>().apply {
        input.onClickDismissAll
            .asFlow()
            .onEach {
                postValue(Event(Unit))
            }
            .launchIn(viewModelScope)
    }

    private fun initOutputIsRefreshing(
        redditPostRepository: RedditPostRepository,
        listOfPosts: LiveData<PagedList<RedditPost>>,
        errorLoadingPage: LiveData<Event<Unit>>
    ) = MutableLiveData(true).apply {
        input.onRefresh
            .asFlow()
            .onEach {
                // Invalidates local in-memory list of posts (causes data to be loaded from the
                // API again next time)
                redditPostRepository.invalidateLocalData()

                // Invalidates the data source (causes a refresh in the paged list):
                val dataSource = listOfPosts.value?.dataSource
                dataSource?.invalidate()

                // Set "isRefreshing" to true when beginning the refresh:
                postValue(true)
            }
            .launchIn(viewModelScope)

        val onLoadCallback = object : PagedList.Callback() {
            override fun onInserted(position: Int, count: Int) {
                postValue(false)
            }

            override fun onRemoved(position: Int, count: Int) {
                postValue(false)
            }

            override fun onChanged(position: Int, count: Int) {
                postValue(false)
            }
        }

        listOfPosts
            .asFlow()
            .onEach {
                // Set "isRefreshing" to false once a page is loaded:
                it.addWeakCallback(null, onLoadCallback)
            }
            .launchIn(viewModelScope)

        errorLoadingPage
            .asFlow()
            .map { false }
            .onEach(::postValue)
            .launchIn(viewModelScope)
    }

    private fun initShowPostDetailsOutput(listOfPosts: LiveData<PagedList<RedditPost>>) =
        MutableLiveData<RedditPost>().apply {
            input.onClickPostListItem
                .asFlow()
                .onEach(::postValue)
                .launchIn(viewModelScope)
        }

    private fun initListOfPostsOutput(
        dataSourceFactory: DataSource.Factory<String, RedditPost>,
        redditPostRepository: RedditPostRepository
    ): LiveData<PagedList<RedditPost>> {
        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .build()

        val pagedList = LivePagedListBuilder(dataSourceFactory, config).build()

        input.onClickDismissPost
            .asFlow()
            .onEach {
                redditPostRepository.filterPost(it)
                pagedList.value?.dataSource?.invalidate()
            }
            .launchIn(viewModelScope)

        input.onClickPostListItem
            .asFlow()
            .onEach {
                redditPostRepository.markPostAsRead(it)
                pagedList.value?.dataSource?.invalidate()
            }
            .launchIn(viewModelScope)

        return pagedList
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}