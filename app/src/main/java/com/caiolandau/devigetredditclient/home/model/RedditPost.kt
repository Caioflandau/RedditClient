package com.caiolandau.devigetredditclient.home.model

import java.net.URL

class RedditPost(
    val title: String,
    val author: String,
    val entryDate: String,
    val thumbnailUrl: URL?,
    val numOfComments: Int,
    val isRead: Boolean
)