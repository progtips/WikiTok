package com.example.wikitok.di

import android.content.Context
import androidx.room.Room
import com.example.wikitok.data.local.LikedArticleDao
import com.example.wikitok.data.local.WikiTokDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): WikiTokDb =
        Room.databaseBuilder(ctx, WikiTokDb::class.java, "wikitok.db").build()

    @Provides
    fun provideLikedArticleDao(db: WikiTokDb): LikedArticleDao = db.likedArticleDao()
}


