package com.caiolandau.devigetredditclient.home.viewmodel

import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.caiolandau.devigetredditclient.api.Api
import com.caiolandau.devigetredditclient.datasource.PagedRedditPostsDataSource
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.repository.RedditPostRepository
import com.caiolandau.devigetredditclient.util.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
        val onClickPostListItem: Channel<Int> = Channel()
    }

    /**
     * Represents outputs - i.e. list of posts - to be presented/handled by the view
     */
    class Output(
        val listOfPosts: LiveData<PagedList<RedditPost>>,
        val showPostDetails: LiveData<Event<RedditPost?>>,
        val errorLoadingPage: LiveData<Event<Unit>>
    )

    val input: Input = Input()
    val output: Output = initOutput(dependency.redditPostRepository)

    private fun initOutput(redditPostRepository: RedditPostRepository): Output {
        val errorLoadingPage = MutableLiveData<Event<Unit>>()
        val dataSourceFactory = PagedRedditPostsDataSource.getFactory(
            redditPostRepository,
            viewModelScope
        ) {
            errorLoadingPage.postValue(Event(Unit))
        }
        val listOfPosts = initListOfPostsOutput(dataSourceFactory)
        val showPostDetails = initShowPostDetailsOutput(listOfPosts)
        return Output(
            listOfPosts = listOfPosts,
            showPostDetails = showPostDetails,
            errorLoadingPage = errorLoadingPage
        )
    }

    private fun initShowPostDetailsOutput(
        listOfPosts: LiveData<PagedList<RedditPost>>
    ) = MutableLiveData<Event<RedditPost?>>().apply {
        viewModelScope.launch {
            input.onClickPostListItem
                .consumeAsFlow()
                .map { position ->
                    Event(listOfPosts.value?.get(position))
                }
                .collect(::postValue)
        }

    }


    private fun initListOfPostsOutput(
        dataSourceFactory: DataSource.Factory<String, RedditPost>
    ): LiveData<PagedList<RedditPost>> {
        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .build()

        return LivePagedListBuilder(dataSourceFactory, config).build()
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}