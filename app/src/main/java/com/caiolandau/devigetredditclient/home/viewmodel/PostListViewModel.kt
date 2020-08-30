package com.caiolandau.devigetredditclient.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.util.SchedulerProvider
import io.reactivex.rxjava3.core.Flowable
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
    }

    /**
     * Represents outputs - i.e. list of posts - to be presented/handled by the view
     */
    class Output(
        val listOfPosts: LiveData<List<RedditPost>>
    )

    val input = Input()
    val output: Output = initOutput(dependency.schedulerProvider)

    private fun initOutput(schedulerProvider: SchedulerProvider) = Output(
        listOfPosts = MutableLiveData<List<RedditPost>>().apply {
            Flowable.just(ArrayList<RedditPost>().apply {
                add(
                    RedditPost(
                        title = "Reddit Post Title",
                        author = "The author",
                        entryDate = "just now",
                        thumbnailUrl = null,
                        numOfComments = 3,
                        isRead = false
                    )
                )
                add(
                    RedditPost(
                        title = "Reddit Post Title 2",
                        author = "The author 2",
                        entryDate = "3 min ago",
                        thumbnailUrl = null,
                        numOfComments = 5,
                        isRead = true
                    )
                )
            })
                .map { it.toList() }
                .delay(8, TimeUnit.SECONDS, schedulerProvider.io())
                .subscribe(::postValue)
        }
    )
}