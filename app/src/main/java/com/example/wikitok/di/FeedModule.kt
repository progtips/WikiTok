package com.example.wikitok.di

import android.content.Context
import com.example.wikitok.domain.Article
import com.example.wikitok.domain.feed.ApiArticlesSource
import com.example.wikitok.data.wiki.wikipediaApi
import com.example.wikitok.domain.feed.ArticlesSource
import com.example.wikitok.domain.feed.FeedBuffer
import com.example.wikitok.domain.feed.IFeedBuffer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    @Singleton
    fun provideArticlesSource(): ArticlesSource = ApiArticlesSource(wikipediaApi)

    @Provides
    @Singleton
    fun provideFeedBuffer(
        @ApplicationContext context: Context,
        source: ArticlesSource
    ): IFeedBuffer {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        return FeedBuffer(
            scope = appScope,
            source = source,
            prefetchSize = 6,
            fetchBatchSize = 6
        )
    }
}


