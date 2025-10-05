package com.example.wikitok.domain.recommend

import com.example.wikitok.domain.Article

class Recommender(private val weights: Map<String, Float>) : IRecommender {
    override fun score(article: Article): Double {
        if (article.categories.isEmpty()) return 0.0
        var s = 0.0
        for (c in article.categories) {
            s += (weights[c] ?: 0f).toDouble()
        }
        return s
    }
}
