package com.caiolandau.devigetredditclient.domain.api

import com.caiolandau.devigetredditclient.domain.api.response.RedditPostsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditApi {
    @GET("top")
    suspend fun getTopPostsTodayPage(
        @Query("limit") limit: Int,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null,
        @Query("t") time: String? = "day"
    ): RedditPostsResponse

    companion object {
        const val baseUrl = "https://api.reddit.com/"
    }
}