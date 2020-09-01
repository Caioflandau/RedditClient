package com.caiolandau.devigetredditclient.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.caiolandau.devigetredditclient.api.Api
import com.caiolandau.devigetredditclient.datasource.PagedRedditPostsDataSource
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.repository.RedditPostRepository
import com.caiolandau.devigetredditclient.util.Event
import com.caiolandau.devigetredditclient.util.SchedulerProvider
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject

class PostListViewModel(
    dependency: Dependency = Dependency()
) : ViewModel() {

    class Dependency(
        val dataSourceFactory: DataSource.Factory<String, RedditPost> =
            object : DataSource.Factory<String, RedditPost>() {
                override fun create(): DataSource<String, RedditPost> {
                    return PagedRedditPostsDataSource(RedditPostRepository(Api().reddit))
                }
            }
    )

    /**
     * Represents input events - i.e. list item clicks - that are possible from the view:
     */
    class Input {
        val onClickPostListItem: PublishSubject<Int> = PublishSubject.create()
    }

    /**
     * Represents outputs - i.e. list of posts - to be presented/handled by the view
     */
    class Output(
        val listOfPosts: LiveData<PagedList<RedditPost>>,
        val showPostDetails: LiveData<Event<RedditPost?>>
    )

    private val compositeDisposable = CompositeDisposable()

    val input: Input = Input()
    val output: Output = initOutput(dependency.dataSourceFactory)

    private fun initOutput(dataSourceFactory: DataSource.Factory<String, RedditPost>): Output {
        val listOfPosts = initListOfPostsOutput(dataSourceFactory)
        val showPostDetails = initShowPostDetailsOutput(listOfPosts)
        return Output(
            listOfPosts = listOfPosts,
            showPostDetails = showPostDetails
        )
    }

    private fun initShowPostDetailsOutput(
        listOfPosts: LiveData<PagedList<RedditPost>>
    ) = MutableLiveData<Event<RedditPost?>>().apply {
        input.onClickPostListItem
            .map { position ->
                Event(listOfPosts.value?.get(position))
            }
            .subscribe(::postValue)
            .addTo(compositeDisposable)
    }


    private fun initListOfPostsOutput(
        dataSourceFactory: DataSource.Factory<String, RedditPost>
    ): LiveData<PagedList<RedditPost>> {
        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .build()

        return LivePagedListBuilder(dataSourceFactory, config).build()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}