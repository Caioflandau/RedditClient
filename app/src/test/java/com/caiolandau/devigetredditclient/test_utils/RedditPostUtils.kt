package com.caiolandau.devigetredditclient.test_utils

import com.caiolandau.devigetredditclient.domain.model.RedditPost
import java.util.*
import kotlin.random.Random

// Simple test utility to make a RedditPost object with random data and/or specific provided fields
fun makeRedditPost(
    id: String = UUID.randomUUID().toString(),
    name: String = UUID.randomUUID().toString(),
    title: String = UUID.randomUUID().toString(),
    author: String = UUID.randomUUID().toString(),
    selfText: String? = UUID.randomUUID().toString(),
    permalink: String = "/r/aaa/${UUID.randomUUID()}",
    entryDate: String = UUID.randomUUID().toString(),
    url: String? = "https://url.com/${UUID.randomUUID()}",
    thumbnailUrl: String? = "https://thumb.url.com/${UUID.randomUUID()}",
    numOfComments: Int = Random.nextInt(),
    isRead: Boolean = false
) = RedditPost(
    id = id,
    name = name,
    title = title,
    author = author,
    selfText = selfText,
    permalink = permalink,
    entryDate = entryDate,
    url = url,
    thumbnailUrl = thumbnailUrl,
    numOfComments = numOfComments,
    isRead = isRead
)