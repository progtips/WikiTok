package com.example.wikitok.di

import android.content.Context
import com.example.wikitok.data.prefs.CategoryWeightsStore
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrefsModule {
    @Provides
    @Singleton
    fun provideCategoryWeightsStore(@ApplicationContext ctx: Context): ICategoryWeightsStore = CategoryWeightsStore(ctx)
}
