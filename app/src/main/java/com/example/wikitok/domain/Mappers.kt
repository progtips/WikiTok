package com.example.wikitok.domain

import com.example.wikitok.data.local.LikedArticleEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Article.toEntity(nowMs: Long = System.currentTimeMillis()): LikedArticleEntity =
    LikedArticleEntity(
        pageId = pageId,
        title = title,
        thumbnailUrl = imageUrl,
        categoriesJson = Json.encodeToString(categories),
        likedAt = nowMs
    )

fun LikedArticleEntity.toDomain(): Article =
    Article(
        pageId = pageId,
        title = title,
        summary = null,
        imageUrl = thumbnailUrl,
        categories = runCatching { Json.decodeFromString<List<String>>(categoriesJson) }.getOrElse { emptyList() }
    )


