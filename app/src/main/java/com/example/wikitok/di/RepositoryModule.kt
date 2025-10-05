package com.example.wikitok.di

import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.data.likes.LikesRepositoryImpl
import com.example.wikitok.data.local.LikedArticleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLikesRepository(dao: LikedArticleDao): ILikesRepository = LikesRepositoryImpl(dao)
}
