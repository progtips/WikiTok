package com.example.wikitok.data.wiki

import kotlinx.serialization.Serializable
import com.example.wikitok.domain.Article as DomainArticle

@Serializable
data class WikiSummaryDto(
    val title: String,
    val extract: String? = null,
    val description: String? = null,
    val thumbnail: Thumbnail? = null,
    val pageid: Long? = null,
    val normalizedtitle: String? = null
) {
    @Serializable
    data class Thumbnail(val source: String? = null)
}


// Маппер в доменную модель
fun WikiSummaryDto.toDomain(categories: List<String> = emptyList()): DomainArticle =
    DomainArticle(
        pageId = this.pageid ?: 0L,
        title = this.title,
        summary = this.extract ?: this.description,
        imageUrl = this.thumbnail?.source,
        categories = categories
    )


