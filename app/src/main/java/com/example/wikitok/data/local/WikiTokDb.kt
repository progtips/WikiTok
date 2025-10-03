package com.example.wikitok.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LikedArticleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WikiTokDb : RoomDatabase() {
    abstract fun likedArticleDao(): LikedArticleDao
}


