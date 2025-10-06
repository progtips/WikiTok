package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article

interface ArticlesSource {
    /** Загружает следующую порцию статей. Пустой список = реально закончились. */
    suspend fun fetchBatch(limit: Int): List<Article>
}


