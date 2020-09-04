package com.caiolandau.devigetredditclient.domain.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class Api(
    moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
    redditBaseUrl: String = RedditApi.baseUrl
) {
    val reddit: RedditApi = Retrofit.Builder()
        .baseUrl(redditBaseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(RedditApi::class.java)
}