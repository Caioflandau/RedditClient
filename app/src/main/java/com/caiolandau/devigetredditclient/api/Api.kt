package com.caiolandau.devigetredditclient.api

class Api(
    retrofitFactory: RetrofitFactory = RetrofitFactory()
) {
    val reddit: RedditApi = retrofitFactory
        .build(RedditApi.baseUrl)
        .create(RedditApi::class.java)
}