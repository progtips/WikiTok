package com.example.wikitok.domain

data class Article(
    val pageId: Long,
    val title: String,
    val summary: String?,
    val imageUrl: String?,
    val categories: List<String>
)


