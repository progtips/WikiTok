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
    val topic: Topic = Topic.OTHER
)

class WikiRepository(private val api: WikipediaApi) {
    suspend fun loadRandom(): Article = api.randomSummary().toArticle()
    suspend fun loadRelated(title: String): List<Article> = api.related(title).pages.map { it.toArticle() }
}

private fun WikiSummaryDto.toArticle() = Article(
    id = title,
    title = title,
    description = description,
    extract = extract,
    imageUrl = thumbnail?.source
)


