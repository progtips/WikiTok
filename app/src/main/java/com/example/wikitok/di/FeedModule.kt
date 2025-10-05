package com.example.wikitok.di

import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.feed.ArticlesSource
import com.example.wikitok.domain.feed.FeedBuffer
import com.example.wikitok.domain.feed.IFeedBuffer
import com.example.wikitok.domain.feed.ApiArticlesSource
import com.example.wikitok.domain.recommend.IRecommender
import com.example.wikitok.domain.recommend.Recommender
import com.example.wikitok.data.wiki.WikipediaApi
import com.example.wikitok.data.wiki.wikipediaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.wikitok.domain.history.IRecentHistory

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    @Singleton
    fun provideArticlesSource(): ArticlesSource = ApiArticlesSource(wikipediaApi)

    @Provides
    fun provideRecommender(store: ICategoryWeightsStore): IRecommender =
        Recommender(weights = run {
            // Подпишемся однократно при создании. Для простоты берём пустую карту.
            emptyMap()
        })

    @Provides
    @Singleton
    fun provideFeedBuffer(source: ArticlesSource, recommender: IRecommender, recent: IRecentHistory): IFeedBuffer =
        FeedBuffer(source, recommender, recentHistory = recent)
}


