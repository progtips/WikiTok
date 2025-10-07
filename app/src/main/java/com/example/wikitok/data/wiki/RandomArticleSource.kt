package com.example.wikitok.data.wiki

import com.example.wikitok.domain.Article
import javax.inject.Inject
import java.util.UUID
import kotlin.math.abs

class RandomArticleSource @Inject constructor(
    private val api: WikiApi
) {
    private fun extractPageId(url: String?): Long {
        if (url.isNullOrBlank()) return abs(UUID.randomUUID().mostSignificantBits)
        val last = url.substringAfterLast('/')
        val numeric = last.toLongOrNull()
        return numeric ?: abs(UUID.nameUUIDFromBytes(last.toByteArray()).mostSignificantBits)
    }

    private fun safeSummary(w: WikiSummary): String {
        return w.extract?.takeIf { it.isNotBlank() }
            ?: w.description?.takeIf { it.isNotBlank() }
            ?: "Короткая статья из Википедии."
    }

    suspend fun fetch(): Article {
        val w = api.randomSummary()

        val desktopUrl = w.content_urls?.desktop?.page
        val pageId = extractPageId(desktopUrl)

        return Article(
            pageId = pageId,
            title = w.title.ifBlank { "Без названия" },
            summary = safeSummary(w),
            imageUrl = w.thumbnail?.source,
            categories = emptyList()
        )
    }

    fun localFallback(): Article = Article(
        pageId = abs(UUID.randomUUID().mostSignificantBits),
        title = "Wikipedia — случайная статья",
        summary = "Интернет недоступен. Показан локальный плейсхолдер, попробуйте обновить.",
        imageUrl = null,
        categories = emptyList()
    )
}


