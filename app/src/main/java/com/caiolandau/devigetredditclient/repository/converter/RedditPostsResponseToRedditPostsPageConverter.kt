package com.caiolandau.devigetredditclient.repository.converter

import com.caiolandau.devigetredditclient.api.response.RedditPostsResponse
import com.caiolandau.devigetredditclient.api.response.RedditPostsResponseData
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.home.model.RedditPostPage
import java.net.URL

/**
 * Class responsible for converting API-layer response objects into app-domain models:
 * RedditPostsResponse -> RedditPostPage
 */
class RedditPostsResponseToRedditPostsPageConverter {
    fun convert(response: RedditPostsResponse) = RedditPostPage(
        posts = response.data.children.map {
            RedditPost(
                title = it.data.title,
                author = it.data.author,
                entryDate = "${it.data.createdUtc}",
                thumbnailUrl = URL(it.data.thumbnail),
                numOfComments = it.data.numComments,
                isRead = false
            )
        }
    )
}