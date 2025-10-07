package com.example.wikitok.data.wiki

import com.example.wikitok.data.WikiRepository
import com.example.wikitok.util.Jsons
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import java.util.Locale

private const val BASE_URL = "https://ru.wikipedia.org/api/rest_v1/"

@OptIn(ExperimentalSerializationApi::class)
private fun createRetrofit(): Retrofit {
    val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val uaInterceptor = Interceptor { chain ->
        val lang = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ru"
        val request = chain.request().newBuilder()
            .header(
                "User-Agent",
                "WikiTok/1.0 (https://wikitok.app; dev@wikitok.app)"
            )
            .header("Accept", "application/json")
            .header("Accept-Language", lang)
            .build()
        chain.proceed(request)
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(uaInterceptor)
        .addInterceptor(logger)
        .retryOnConnectionFailure(true)
        .build()

    val json = Jsons.default

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


