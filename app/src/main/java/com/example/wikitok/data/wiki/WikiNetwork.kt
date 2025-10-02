package com.example.wikitok.data.wiki

import com.example.wikitok.data.WikiRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType

private const val BASE_URL = "https://ru.wikipedia.org/api/rest_v1/"

@OptIn(ExperimentalSerializationApi::class)
private fun createRetrofit(): Retrofit {
    val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    val contentType = "application/json".toMediaType()

    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}

private val retrofit: Retrofit = createRetrofit()

val wikipediaApi: WikipediaApi = retrofit.create(WikipediaApi::class.java)

fun provideRepository(): WikiRepository = WikiRepository(wikipediaApi)


