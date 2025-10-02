package com.example.wikitok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.Article
import com.example.wikitok.data.WikiRepository
import com.example.wikitok.data.wiki.provideRepository
import com.example.wikitok.personalization.TopicClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repo: WikiRepository = provideRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<Article>>(emptyList())
    val items: StateFlow<List<Article>> = _items

    private val dwell = mutableMapOf<String, Long>()

    init { viewModelScope.launch { prime(5) } }

    suspend fun prime(n: Int) {
        val batch = mutableListOf<Article>()
        repeat(n) {
            val a = repo.loadRandom()
            val topic = TopicClassifier.classify(a)
            batch += a.copy(topic = topic)
        }
        _items.value = _items.value + batch
    }

    fun onNeedMore() { viewModelScope.launch { prime(5) } }

    fun onImpression(id: String, ms: Long) {
        dwell[id] = (dwell[id] ?: 0L) + ms
    }
}


