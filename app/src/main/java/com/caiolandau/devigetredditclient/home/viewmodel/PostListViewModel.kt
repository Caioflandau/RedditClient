package com.caiolandau.devigetredditclient.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.util.Event
import com.caiolandau.devigetredditclient.util.SchedulerProvider
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class PostListViewModel(
    dependency: Dependency = Dependency()
) : ViewModel() {

    class Dependency(
        val schedulerProvider: SchedulerProvider = SchedulerProvider()
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
    val output: Output = initOutput()

    private fun initOutput(): Output {
        val listOfPosts = initListOfPostsOutput()
        return Output(
            listOfPosts = listOfPosts,
            showPostDetails = MutableLiveData<Event<RedditPost?>>().apply {
                input.onClickPostListItem
                    .map { position ->
                        Event(listOfPosts.value?.get(position))
                    }
                    .subscribe(::postValue)
                    .addTo(compositeDisposable)
            }
        )
    }

    private fun initListOfPostsOutput() = MutableLiveData<List<RedditPost>>().apply {
        Flowable.just(ArrayList<RedditPost>().apply {
            add(
                RedditPost(
                    title = "Reddit Post Title",
                    author = "Author_1",
                    entryDate = "just now",
                    thumbnailUrl = null,
                    numOfComments = 3,
                    isRead = false
                )
            )
            add(
                RedditPost(
                    title = "Reddit Post Title 2 - Very long lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua",
                    author = "Author_2",
                    entryDate = "3 min ago",
                    thumbnailUrl = null,
                    numOfComments = 5,
                    isRead = true
                )
            )
        })
            .map { it.toList() }
            .subscribe(::postValue)
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}