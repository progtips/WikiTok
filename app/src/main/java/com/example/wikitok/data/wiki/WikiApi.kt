package com.example.wikitok.data.wiki

import retrofit2.http.GET

interface WikiApi {
    @GET("page/random/summary")
    suspend fun randomSummary(): WikiSummary
}


