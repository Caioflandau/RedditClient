package com.caiolandau.devigetredditclient.domain.repository.converter

import android.text.Html
import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponse
import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponseChild
import com.caiolandau.devigetredditclient.domain.model.RedditPost
import com.caiolandau.devigetredditclient.domain.model.RedditPostPage

/**
 * Class responsible for converting API-layer response objects into app-domain models:
 * RedditPostsResponse -> RedditPostPage
 */
class RedditPostsResponseToRedditPostsPageConverter(
    private val epochToRelativeTimeConverter: EpochToRelativeTimeConverter = EpochToRelativeTimeConverter()
) {
    fun convert(
        response: RedditPostsResponse
    ): RedditPostPage {
        return RedditPostPage(
            pageAfter = response.data.after,
            pageBefore = response.data.before,
            posts = response.data.children.map {
                RedditPost(
                    id = it.data.id,
                    name = it.data.name,
                    title = it.data.title,
                    author = it.data.author,
                    entryDate = epochToRelativeTimeConverter.convert(it.data.createdUtc),
                    thumbnailUrl = getThumbnailUrl(it),
                    numOfComments = it.data.numComments,
                    isRead = false
                )
            }
        )
    }

    private fun getThumbnailUrl(responseChild: RedditPostsResponseChild): String? {
        val preview = responseChild.data.preview
        if (preview != null) {
            // For some unknown reason, image preview URLs are HTML-encoded in the JSON.
            // This decodes it. Example: &amp; becomes &
            return Html.fromHtml(preview.images.firstOrNull()?.source?.url, 0)
                .toString()
        }

        // If there's no preview image, use the root-level thumbnail as a fall-back:
        return responseChild.data.thumbnail
    }
}