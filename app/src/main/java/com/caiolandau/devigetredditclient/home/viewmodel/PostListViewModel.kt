package com.caiolandau.devigetredditclient.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
        val schedulerProvider: SchedulerProvider = SchedulerProvider(),
        val redditPostRepository: RedditPostRepository = RedditPostRepository()
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
        val listOfPosts: LiveData<List<RedditPost>>,
        val showPostDetails: LiveData<Event<RedditPost?>>
    )

    private val compositeDisposable = CompositeDisposable()

    val input: Input = Input()
    val output: Output = initOutput(dependency.redditPostRepository)

    private fun initOutput(redditPostRepository: RedditPostRepository): Output {
        val listOfPosts = initListOfPostsOutput(redditPostRepository)
        val showPostDetails = initShowPostDetails(listOfPosts)
        return Output(
            listOfPosts = listOfPosts,
            showPostDetails = showPostDetails
        )
    }

    private fun initShowPostDetails(
        listOfPosts: MutableLiveData<List<RedditPost>>
    ) = MutableLiveData<Event<RedditPost?>>().apply {
        input.onClickPostListItem
            .map { position ->
                Event(listOfPosts.value?.get(position))
            }
            .subscribe(::postValue)
            .addTo(compositeDisposable)
    }


    private fun initListOfPostsOutput(
        redditPostRepository: RedditPostRepository
    ) = MutableLiveData<List<RedditPost>>().apply {
        redditPostRepository.topPostsPage(10)
            .map { it.posts }
            .subscribe(::postValue)
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}