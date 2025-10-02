package com.example.wikitok.data.wiki

import kotlinx.serialization.Serializable

@Serializable
data class WikiSummaryDto(
    val title: String,
    val extract: String? = null,
    val description: String? = null,
    val thumbnail: Thumbnail? = null
) {
    @Serializable
    data class Thumbnail(val source: String? = null)
}


