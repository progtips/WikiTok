package com.example.wikitok.domain.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.wikitok.data.prefs.StringDataSerializer
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import com.example.wikitok.util.Jsons
import java.util.ArrayDeque

private val Context.recentHistoryStore: DataStore<String> by dataStore(
    fileName = "recent_history.json",
    serializer = StringDataSerializer
)

@Serializable
private data class RecentDto(val ids: List<Long> = emptyList())

class RecentHistoryStore(private val context: Context, private val capacity: Int = 50) : IRecentHistory {
    private val json = Jsons.default
    private val memory = ArrayDeque<Long>()

    override suspend fun addShown(pageId: Long) {
        if (memory.contains(pageId)) return
        memory.addFirst(pageId)
        while (memory.size > capacity) memory.removeLast()
        persist()
    }

    override suspend fun wasRecentlyShown(pageId: Long): Boolean {
        if (memory.contains(pageId)) return true
        // lazy load from disk if memory empty
        if (memory.isEmpty()) restore()
        return memory.contains(pageId)
    }

    private suspend fun restore() {
        val raw = context.recentHistoryStore.data.first()
        if (raw.isBlank()) return
        runCatching {
            val dto = json.decodeFromString(RecentDto.serializer(), raw)
            memory.clear()
            dto.ids.asReversed().forEach { memory.addFirst(it) }
        }
    }

    private suspend fun persist() {
        val dto = RecentDto(memory.toList())
        val raw = json.encodeToString(RecentDto.serializer(), dto)
        context.recentHistoryStore.updateData { raw }
    }
}
