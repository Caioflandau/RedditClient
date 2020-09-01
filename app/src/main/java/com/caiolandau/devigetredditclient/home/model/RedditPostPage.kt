package com.caiolandau.devigetredditclient.home.model

class RedditPostPage(
    val posts: List<RedditPost>,
    val pageAfter: String?,
    val pageBefore: String?
)