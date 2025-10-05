package com.example.wikitok.data.prefs

import kotlinx.coroutines.flow.Flow

interface ICategoryWeightsStore {
    suspend fun bumpPositive(categories: List<String>, delta: Float = 0.2f)
    suspend fun bumpNegative(categories: List<String>, delta: Float = -0.1f)
    fun observeWeights(): Flow<Map<String, Float>>
    suspend fun getTopCategories(limit: Int = 10): List<String>
}
