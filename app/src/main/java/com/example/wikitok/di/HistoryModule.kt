package com.example.wikitok.di

import android.content.Context
import com.example.wikitok.domain.history.IRecentHistory
import com.example.wikitok.domain.history.RecentHistoryStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HistoryModule {
    @Provides
    @Singleton
    fun provideRecentHistory(@ApplicationContext ctx: Context): IRecentHistory = RecentHistoryStore(ctx)
}


