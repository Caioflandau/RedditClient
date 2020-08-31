package com.caiolandau.devigetredditclient.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * The classes in this file are 1:1 mappings between the API's JSON and Kotlin classes.
 * The only purpose they have is to represent the JSON data as Kotlin objects. They are converted by
 * the repository (using a converter) to app-domain models - i.e. RedditPostPage
 */

open class RedditPostsResponse(
    @Json(name = "kind")
    val kind: String,

    @Json(name = "data")
    val data: RedditPostsResponseData
)

open class RedditPostsResponseData(
    @Json(name = "children")
    val children: List<RedditPostsResponseChild>
)

open class RedditPostsResponseChild(
    @Json(name = "data")
    val data: RedditPostsResponsePostData
)

open class RedditPostsResponsePostData(
    @Json(name = "title")
    val title: String,

    @Json(name = "author")
    val author: String,

    @Json(name = "thumbnail")
    val thumbnail: String?,

    @Json(name = "created_utc")
    val createdUtc: Double,

    @Json(name = "url")
    val url: String?,

    @Json(name = "num_comments")
    val numComments: Int
)