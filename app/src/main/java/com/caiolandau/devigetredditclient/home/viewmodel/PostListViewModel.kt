package com.caiolandau.devigetredditclient.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.caiolandau.devigetredditclient.api.RedditApi
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.repository.RedditPostRepository
import com.caiolandau.devigetredditclient.util.Event
import com.caiolandau.devigetredditclient.util.SchedulerProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class PostListViewModel(
    dependency: Dependency = Dependency()
) : ViewModel() {

    class Dependency(
        val schedulerProvider: SchedulerProvider = SchedulerProvider(),
        val redditPostRepository: RedditPostRepository = RedditPostRepository(
            redditApi = Retrofit.Builder()
                .baseUrl("https://api.reddit.com/")
                .addConverterFactory(MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                ))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(RedditApi::class.java)
        )
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
        val showPostDetails = initShowPostDetailsOutput(listOfPosts)
        return Output(
            listOfPosts = listOfPosts,
            showPostDetails = showPostDetails
        )
    }

    private fun initShowPostDetailsOutput(
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
        redditPostRepository.topPostsTodayPage(10)
            .map { it.posts }
            .subscribe(::postValue)
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}