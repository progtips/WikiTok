package com.example.wikitok.data.wiki

import kotlinx.serialization.Serializable

@Serializable
data class WikiSummary(
    val title: String,
    val description: String? = null,
    val extract: String? = null,
    val content_urls: ContentUrls? = null,
    val thumbnail: Thumbnail? = null
) {
    @Serializable
    data class ContentUrls(val desktop: Urls? = null, val mobile: Urls? = null) {
        @Serializable
        data class Urls(val page: String? = null)
    }
    @Serializable
    data class Thumbnail(val source: String? = null)
}


