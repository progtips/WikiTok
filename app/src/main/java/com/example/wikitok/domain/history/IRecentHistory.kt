package com.example.wikitok.domain.history

interface IRecentHistory {
    suspend fun addShown(pageId: Long)
    suspend fun wasRecentlyShown(pageId: Long): Boolean
}
