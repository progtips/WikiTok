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
import com.example.wikitok.personalization.loadLanguage
import com.example.wikitok.personalization.saveLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import android.content.Context

class FeedViewModel(
    private val repo: WikiRepository = provideRepository()
) : ViewModel() {
    private val _items = MutableStateFlow<List<Article>>(emptyList())
    val items: StateFlow<List<Article>> = _items

    private val dwell = mutableMapOf<String, Long>()
    val bandit = EpsilonGreedy()

    init { viewModelScope.launch { prime(5) } }

    @OptIn(FlowPreview::class)
    fun attach(context: Context) {
        // restore
        viewModelScope.launch {
            val restored = loadBandit(context)
            if (restored.isNotEmpty()) bandit.restore(restored)
            loadLanguage(context)?.let { _language.value = it }
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

    private val _language = MutableStateFlow("ru")
    val language: StateFlow<String> = _language

    fun changeLanguage(context: Context, lang: String) {
        _language.value = lang
        viewModelScope.launch { saveLanguage(context, lang) }
        // Перезагрузить текущий пул, чтобы применить язык
        viewModelScope.launch {
            prime(3)
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
        // Не меняем порядок уже показанных карточек; добавляем новую пачку, отсортированную бандитом, в конец
        val sortedNew = batch.sortedByDescending { bandit.score(it.topic) }
        _items.value = _items.value + sortedNew
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
        // Сортируем только новую подмешанную группу и добавляем в конец, не трогая существующий порядок
        val sortedNew = newItems.sortedByDescending { bandit.score(TopicClassifier.classify(it)) }
        _items.value = _items.value + sortedNew
    }
}


