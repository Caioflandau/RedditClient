package com.caiolandau.devigetredditclient.redditpostlist.viewmodel

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

typealias MakePagedListLivedata = (
    DataSource.Factory<String, RedditPost>,
    PagedList.Config
) -> LiveData<PagedList<RedditPost>>

class PostListViewModel(
    dependency: Dependency = Dependency(),
    private val makePagedListLiveData: MakePagedListLivedata = { dataSourceFactory, config ->
        LivePagedListBuilder(dataSourceFactory, config).build()
    }
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
        val showPostDetails = initShowPostDetailsOutput()
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
        clearedAll: LiveData<Event<Unit>>,
        showPostDetails: LiveData<RedditPost>
    ) = merge(
        // When dismissing the currently showing post:
        input.onClickDismissPost.asFlow()
            .combine(showPostDetails.asFlow()) { dismissedPost, currentDetailsPost ->
                Pair(dismissedPost, currentDetailsPost)
            }
            .filter { it.first.id == it.second.id },

        // When cleared all posts:
        clearedAll.asFlow()
    ).map { Event(Unit) }.asLiveData(viewModelScope.coroutineContext)

    private fun initOutputClearedAll() = input.onClickDismissAll.asFlow()
        .map(::Event)
        .asLiveData(viewModelScope.coroutineContext)

    private fun initOutputIsRefreshing(
        redditPostRepository: RedditPostRepository,
        listOfPosts: LiveData<PagedList<RedditPost>>,
        errorLoadingPage: LiveData<Event<Unit>>
    ): LiveData<Boolean> {
        val onRefreshFlow = input.onRefresh.asFlow()
            .onEach {
                // Invalidates local in-memory list of posts (causes data to be loaded from the
                // API again next time)
                redditPostRepository.invalidateLocalData()

                // Invalidates the data source (causes a refresh in the paged list):
                val dataSource = listOfPosts.value?.dataSource
                dataSource?.invalidate()
            }
            .map { true }

        val onErrorLoadingPageFlow = errorLoadingPage.asFlow()
            .map { false }

        val listOfPostsLiveData = MutableLiveData(true).apply {
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
        }

        return MediatorLiveData<Boolean>().apply {
            addSource(listOfPostsLiveData) {
                this.value = it
            }

            addSource(
                merge(onRefreshFlow, onErrorLoadingPageFlow)
                    .asLiveData(viewModelScope.coroutineContext)
            ) { this.value = it }
        }
    }

    private fun initShowPostDetailsOutput() =
        input.onClickPostListItem.asFlow()
            .asLiveData(viewModelScope.coroutineContext)

    private fun initListOfPostsOutput(
        dataSourceFactory: DataSource.Factory<String, RedditPost>,
        redditPostRepository: RedditPostRepository
    ): LiveData<PagedList<RedditPost>> {
        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .build()

        val pagedList = makePagedListLiveData(dataSourceFactory, config)

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

    override fun onCleared() {
        super.onCleared()
        input.onClickDismissAll.close()
        input.onClickDismissPost.close()
        input.onClickPostListItem.close()
        input.onRefresh.close()
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}