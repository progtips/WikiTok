package com.example.wikitok.data

import com.example.wikitok.data.wiki.WikiSummaryDto
import com.example.wikitok.data.wiki.WikipediaApi
import com.example.wikitok.personalization.Topic

data class Article(
    val id: String,         // title
    val title: String,
    val description: String?,
    val extract: String?,
    val imageUrl: String?,
    val topic: Topic = Topic.OTHER,
    val url: String
)

class WikiRepository(private val api: WikipediaApi) {
    suspend fun loadRandom(): Article = api.randomSummary().toArticle()
    suspend fun loadRelated(title: String): List<Article> = api.related(title).pages.map { it.toArticle() }
}

private fun WikiSummaryDto.toArticle(): Article {
    val encoded = try {
        java.net.URLEncoder.encode(title, Charsets.UTF_8.name()).replace('+', '_')
    } catch (_: Throwable) {
        title.replace(' ', '_')
    }
    val lang = java.util.Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ru"
    val link = "https://${lang}.wikipedia.org/wiki/${encoded}"
    return Article(
        id = title,
        title = title,
        description = description,
        extract = extract,
        imageUrl = thumbnail?.source,
        url = link
    )
}


