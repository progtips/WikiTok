package com.example.wikitok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.Article
import com.example.wikitok.data.WikiRepository
import com.example.wikitok.data.wiki.provideRepository
import com.example.wikitok.personalization.EpsilonGreedy
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
    val bandit = EpsilonGreedy()

    init { viewModelScope.launch { prime(5) } }

    suspend fun prime(n: Int) {
        val batch = mutableListOf<Article>()
        repeat(n) {
            try {
                val a = repo.loadRandom()
                val topic = TopicClassifier.classify(a)
                batch += a.copy(topic = topic)
            } catch (t: Throwable) {
                // пропускаем ошибочную загрузку, не падаем
            }
        }
        val current = _items.value
        val viewed = current.filter { dwell.containsKey(it.id) }
        val unviewedPool = current.filterNot { dwell.containsKey(it.id) } + batch
        val enriched = unviewedPool.map { it.copy(topic = TopicClassifier.classify(it)) }
        val sorted = enriched.sortedByDescending { bandit.score(it.topic) }
        _items.value = viewed + sorted
    }

    fun onNeedMore() { viewModelScope.launch { prime(5) } }

    fun onImpression(id: String, ms: Long) {
        dwell[id] = (dwell[id] ?: 0L) + ms
        val article = _items.value.find { it.id == id } ?: return
        val dwellSec = (dwell[id] ?: 0L) / 1000.0
        val norm = kotlin.math.min(dwellSec / 15.0, 1.0)
        bandit.update(article.topic, norm)
    }

    fun onLike(id: String) {
        val article = _items.value.find { it.id == id } ?: return
        bandit.update(article.topic, 1.0)
    }
}


