package com.caiolandau.devigetredditclient.domain.api.response

import com.squareup.moshi.Json

/**
 * The classes in this file are 1:1 mappings between the API's JSON and Kotlin classes.
 * The only purpose they have is to represent the JSON data as Kotlin objects. They are converted by
 * the repository (using a converter) to app-domain models - i.e. RedditPostPage
 */

class RedditPostsResponse(
    @Json(name = "kind")
    val kind: String,

    @Json(name = "data")
    val data: RedditPostsResponseData
)

class RedditPostsResponseData(
    @Json(name = "children")
    val children: List<RedditPostsResponseChild>,

    @Json(name = "after")
    val after: String?,

    @Json(name = "before")
    val before: String?
)

class RedditPostsResponseChild(
    @Json(name = "data")
    val data: RedditPostsResponsePostData
)

class RedditPostsResponsePostData(
    @Json(name = "id")
    val id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "selftext")
    val selfText: String?,

    @Json(name = "permalink")
    val permalink: String,

    @Json(name = "author")
    val author: String,

    @Json(name = "thumbnail")
    val thumbnail: String?,

    @Json(name = "created_utc")
    val createdUtc: Long,

    @Json(name = "url")
    val url: String?,

    @Json(name = "num_comments")
    val numComments: Int,

    @Json(name = "preview")
    val preview: RedditPostsResponsePostPreview?
)

class RedditPostsResponsePostPreview(
    @Json(name = "images")
    val images: List<RedditPostsResponsePostPreviewImage>
)

class RedditPostsResponsePostPreviewImage(
    @Json(name = "source")
    val source: RedditPostsResponsePostPreviewImageSource
)

class RedditPostsResponsePostPreviewImageSource(
    @Json(name = "url")
    val url: String
)
