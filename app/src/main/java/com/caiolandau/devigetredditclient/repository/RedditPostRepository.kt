package com.caiolandau.devigetredditclient.repository

import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.home.model.RedditPostPage
import io.reactivex.rxjava3.core.Single

class RedditPostRepository {
    fun topPostsPage(numOfItems: Int, after: String? = null): Single<RedditPostPage> {
        return Single.just(
            RedditPostPage(posts = ArrayList<RedditPost>().apply {
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
        )
    }
}