package com.example.wikitok.data.wiki

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface WikipediaApi {
    @GET("page/random/summary")
    suspend fun randomSummary(
        @Header("Accept-Language") lang: String = "ru"
    ): WikiSummaryDto

    @GET("page/related/{title}")
    suspend fun related(
        @Path("title") title: String,
        @Header("Accept-Language") lang: String = "ru"
    ): RelatedResponse
}

@Serializable
data class RelatedResponse(val pages: List<WikiSummaryDto> = emptyList())


