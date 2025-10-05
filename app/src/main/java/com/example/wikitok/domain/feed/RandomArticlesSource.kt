package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article
import kotlin.random.Random

class RandomArticlesSource : ArticlesSource {
    private val categories = listOf("history","science","art","tech","sport")

    override suspend fun fetchBatch(n: Int): List<Article> {
        val list = ArrayList<Article>(n)
        repeat(n) {
            val cats = categories.shuffled().take(Random.nextInt(1, 3))
            val id = Random.nextLong(1, 1_000_000)
            list += Article(
                pageId = id,
                title = "Article $id",
                summary = null,
                imageUrl = null,
                categories = cats
            )
        }
        return list
    }
}
