package com.caiolandau.devigetredditclient.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RetrofitFactory {
    fun build(
        baseUrl: String,
        moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}