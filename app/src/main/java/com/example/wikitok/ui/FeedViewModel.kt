package com.example.wikitok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.Article
import com.example.wikitok.data.WikiRepository
import com.example.wikitok.data.wiki.provideRepository
import com.example.wikitok.personalization.EpsilonGreedy
import com.example.wikitok.personalization.TopicClassifier
import com.example.wikitok.personalization.loadBandit
import com.example.wikitok.personalization.saveBandit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context

class FeedViewModel(
    private val repo: WikiRepository = provideRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<Article>>(emptyList())
    val items: StateFlow<List<Article>> = _items

    private val dwell = mutableMapOf<String, Long>()
    val bandit = EpsilonGreedy()

    init { viewModelScope.launch { prime(5) } }

    fun attach(context: Context) {
        // restore
        viewModelScope.launch {
            val restored = loadBandit(context)
            if (restored.isNotEmpty()) bandit.restore(restored)
        }
        // save with debounce 3s on items changes (as a proxy for updates)
        viewModelScope.launch {
            items
                .debounce(3000)
                .distinctUntilChanged()
                .collect {
                    saveBandit(context, bandit.snapshot())
                }
        }
    }

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
        onLike(article)
    }

    fun onLike(a: Article) {
        updateReward(a, like = true)
        viewModelScope.launch {
            try {
                val rel = repo.loadRelated(a.title).take(3)
                addAndResort(rel)
            } catch (_: Throwable) {
                // игнорируем ошибку сети при подмешивании
            }
        }
    }

    fun onDislike(a: Article) {
        updateReward(a, like = false)
    }

    private fun updateReward(a: Article, like: Boolean) {
        val dwellMs = dwell[a.id] ?: 0L
        val dwellSec = dwellMs / 1000.0
        val norm = (dwellSec / 15.0).coerceIn(0.0, 1.0)
        val reward = norm + if (like) 1.0 else 0.0
        bandit.update(TopicClassifier.classify(a), reward)
    }

    private fun addAndResort(newItems: List<Article>) {
        val merged = _items.value + newItems
        val resorted = merged.sortedByDescending { bandit.score(TopicClassifier.classify(it)) }
        _items.value = resorted
    }
}


