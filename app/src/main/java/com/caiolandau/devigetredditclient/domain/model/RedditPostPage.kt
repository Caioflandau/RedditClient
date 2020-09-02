package com.caiolandau.devigetredditclient.domain.model

class RedditPostPage(
    val posts: List<RedditPost>,
    val pageAfter: String?,
    val pageBefore: String?
)