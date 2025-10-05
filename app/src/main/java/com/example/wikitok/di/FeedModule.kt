package com.example.wikitok.di

import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.feed.ArticlesSource
import com.example.wikitok.domain.feed.FeedBuffer
import com.example.wikitok.domain.feed.IFeedBuffer
import com.example.wikitok.domain.feed.RandomArticlesSource
import com.example.wikitok.domain.recommend.IRecommender
import com.example.wikitok.domain.recommend.Recommender
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    @Singleton
    fun provideArticlesSource(): ArticlesSource = RandomArticlesSource()

    @Provides
    fun provideRecommender(store: ICategoryWeightsStore): IRecommender =
        Recommender(weights = run {
            // Подпишемся однократно при создании. Для простоты берём пустую карту.
            emptyMap()
        })

    @Provides
    @Singleton
    fun provideFeedBuffer(source: ArticlesSource, recommender: IRecommender): IFeedBuffer =
        FeedBuffer(source, recommender)
}


