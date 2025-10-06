package com.example.wikitok.di

import com.example.wikitok.data.wiki.wikipediaApi
import com.example.wikitok.domain.feed.ApiArticlesSource
import com.example.wikitok.domain.feed.ArticlesSource
import com.example.wikitok.domain.feed.FeedBuffer
import com.example.wikitok.domain.feed.IFeedBuffer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    @Singleton
    fun provideArticlesSource(): ArticlesSource = ApiArticlesSource(wikipediaApi)

    @Provides
    @Singleton
    fun provideFeedBuffer(source: ArticlesSource): IFeedBuffer {
        return FeedBuffer(
            source = source,
            batchSize = 10
        )
    }
}


