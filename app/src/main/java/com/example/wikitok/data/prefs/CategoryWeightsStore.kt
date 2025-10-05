package com.example.wikitok.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.categoryWeightsDataStore: DataStore<String> by dataStore(
    fileName = "category_weights.json",
    serializer = StringDataSerializer
)

@Serializable
private data class WeightsDto(val map: Map<String, Float> = emptyMap())

class CategoryWeightsStore(private val context: Context) : ICategoryWeightsStore {
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeWeights(): Flow<Map<String, Float>> =
        context.categoryWeightsDataStore.data.map { raw ->
            if (raw.isBlank()) emptyMap() else
                runCatching { json.decodeFromString(WeightsDto.serializer(), raw).map }
                    .getOrElse { emptyMap() }
        }

    override suspend fun getTopCategories(limit: Int): List<String> {
        val map = observeWeights().first()
        return map.entries.sortedByDescending { it.value }.take(limit).map { it.key }
    }

    override suspend fun bumpPositive(categories: List<String>, delta: Float) {
        bumpInternal(categories, delta.coerceIn(-1f, 1f))
    }

    override suspend fun bumpNegative(categories: List<String>, delta: Float) {
        bumpInternal(categories, delta.coerceIn(-1f, 1f))
    }

    private suspend fun bumpInternal(categories: List<String>, delta: Float) {
        val current = observeWeights().first()
        val updated = current.toMutableMap()
        for (c in categories) {
            val v = (updated[c] ?: 0f) + delta
            updated[c] = v.coerceIn(-1f, 1f)
        }
        val dto = WeightsDto(updated)
        val raw = json.encodeToString(WeightsDto.serializer(), dto)
        context.categoryWeightsDataStore.updateData { raw }
    }

    override suspend fun setWeight(category: String, value: Float) {
        val current = observeWeights().first()
        val updated = current.toMutableMap()
        updated[category] = value.coerceIn(-1f, 1f)
        val dto = WeightsDto(updated)
        val raw = json.encodeToString(WeightsDto.serializer(), dto)
        context.categoryWeightsDataStore.updateData { raw }
    }

    override suspend fun resetAll() {
        val dto = WeightsDto(emptyMap())
        val raw = json.encodeToString(WeightsDto.serializer(), dto)
        context.categoryWeightsDataStore.updateData { raw }
    }
}
