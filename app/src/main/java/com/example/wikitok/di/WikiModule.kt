package com.example.wikitok.di

import com.example.wikitok.data.wiki.WikiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.wikitok.util.Jsons
import okhttp3.OkHttpClient
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object WikiModule {

    private val json = Jsons.default

    @Provides
    @Singleton
    fun provideOkHttp(@ApplicationContext ctx: android.content.Context): OkHttpClient {
        val ua = "WikiTok/1.0 (https://example.com; contact: dev@wikitok.app)"
        val headerInterceptor = okhttp3.Interceptor { chain ->
            val req = chain.request().newBuilder()
                .header("User-Agent", ua)
                .header("Accept", "application/json")
                .build()
            chain.proceed(req)
        }
        val logger = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logger)
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .cache(okhttp3.Cache(java.io.File(ctx.cacheDir, "okhttp"), 20L * 1024 * 1024))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://ru.wikipedia.org/api/rest_v1/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideWikiApi(retrofit: Retrofit): WikiApi = retrofit.create(WikiApi::class.java)
}


