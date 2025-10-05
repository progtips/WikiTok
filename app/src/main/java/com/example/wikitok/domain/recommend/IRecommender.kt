package com.example.wikitok.domain.recommend

import com.example.wikitok.domain.Article

interface IRecommender {
    fun score(article: Article): Double
}
