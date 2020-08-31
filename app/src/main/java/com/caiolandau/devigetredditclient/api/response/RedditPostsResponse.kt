package com.caiolandau.devigetredditclient.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
    val createdUtc: Int,

    @Json(name = "url")
    val url: String?,

    @Json(name = "num_comments")
    val numComments: Int
)