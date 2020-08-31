package com.caiolandau.devigetredditclient.api

import com.caiolandau.devigetredditclient.api.response.RedditPostsResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditApi {
    @GET("top")
    fun getTopPosts(@Query("limit") limit: Int, @Query("t") time: String): Single<RedditPostsResponse>

    companion object {
        const val baseUrl = "https://api.reddit.com/"
    }
}