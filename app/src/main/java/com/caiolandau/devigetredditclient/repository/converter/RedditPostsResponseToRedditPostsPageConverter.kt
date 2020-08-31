package com.caiolandau.devigetredditclient.repository.converter

import android.text.format.DateUtils
import com.caiolandau.devigetredditclient.api.response.RedditPostsResponse
import com.caiolandau.devigetredditclient.home.model.RedditPost
import com.caiolandau.devigetredditclient.home.model.RedditPostPage
import java.net.URL
import java.util.*

/**
 * Class responsible for converting API-layer response objects into app-domain models:
 * RedditPostsResponse -> RedditPostPage
 */
class RedditPostsResponseToRedditPostsPageConverter(
    private val epochToRelativeTimeConverter: EpochToRelativeTimeConverter = EpochToRelativeTimeConverter()
) {
    fun convert(response: RedditPostsResponse) = RedditPostPage(
        posts = response.data.children.map {
            RedditPost(
                title = it.data.title,
                author = it.data.author,
                entryDate = epochToRelativeTimeConverter.convert(it.data.createdUtc),
                thumbnailUrl = URL(it.data.thumbnail),
                numOfComments = it.data.numComments,
                isRead = false
            )
        }
    )
}