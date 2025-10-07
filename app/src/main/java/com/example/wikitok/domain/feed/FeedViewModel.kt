package com.example.wikitok.domain.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.wikitok.data.wiki.RandomArticleSource
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.ArrayDeque

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val likesRepository: ILikesRepository,
    private val preferencesStore: ICategoryWeightsStore,
    private val feedBuffer: IFeedBuffer,
    private val randomSource: RandomArticleSource
) : ViewModel() {

    private val _current = MutableStateFlow<Article?>(null)
    val current: StateFlow<Article?> = _current

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _isFetchingNext = MutableStateFlow(false)
    val isFetchingNext: StateFlow<Boolean> = _isFetchingNext

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError

    @OptIn(ExperimentalCoroutinesApi::class)
    val isLikedCurrent: StateFlow<Boolean> = _current
        .flatMapLatest { a ->
            val id = a?.pageId ?: -1L
            if (id <= 0) flowOf(false) else likesRepository.isLiked(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val loadMutex = Mutex()
    private val history = ArrayDeque<Article>()

    private val _prev = MutableStateFlow<Article?>(null)
    val prev: StateFlow<Article?> = _prev

    private val _nextPreview = MutableStateFlow<Article?>(null)
    val nextPreview: StateFlow<Article?> = _nextPreview

    init {
        // 1) Быстрый первый кадр: мгновенно тянем случайную статью и снимаем initialLoading, даже при ошибке
        viewModelScope.launch {
            android.util.Log.d("FeedVM", "TurboStart: fetching random page...")
            val first = runCatching { randomSource.fetch() }
                .onFailure { 
                    android.util.Log.e("FeedVM", "TurboStart failed", it)
                    _lastError.value = it.message ?: it::class.java.simpleName
                }
                .getOrNull()
            _current.value = first ?: runCatching { randomSource.localFallback() }.getOrNull()
            _isInitialLoading.value = false
        }
        // 2) Фоновая подкачка буфера
        viewModelScope.launch {
            runCatching { feedBuffer.primeIfNeeded() }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _lastError.value = null
            val result = runCatching { randomSource.fetch() }
                .onFailure { _lastError.value = it.message ?: it::class.java.simpleName }
                .getOrNull()
            if (result != null) {
                _current.value = result
            }
        }
    }

    fun loadNext() {
        viewModelScope.launch { loadNextInternal() }
    }

    private suspend fun loadNextInternal() {
        val firstLoad = _current.value == null
        if (firstLoad) _isInitialLoading.value = true else _isFetchingNext.value = true
        val t0 = android.os.SystemClock.elapsedRealtime()

        try {
            loadMutex.withLock {
                // 1) Первая попытка: prime + next
                val primeResult = runCatching { feedBuffer.primeIfNeeded() }
                if (primeResult.isFailure) {
                    _error.value = primeResult.exceptionOrNull()?.message ?: "prime_failed"
                    return@withLock
                }

                var next: Article? = runCatching { feedBuffer.next() }.getOrNull()

                // 2) Автоматический повтор: ещё раз prime + next, если пусто
                if (next == null) {
                    runCatching { feedBuffer.primeIfNeeded() }
                    next = runCatching { feedBuffer.next() }.getOrNull()
                }

                if (next == null) {
                    _error.value = "empty_feed"
                    android.util.Log.w("Feed", "empty_feed after two attempts")
                    return@withLock
                }

                // Успех: сохраняем предыдущую статью в историю и показываем новую
                _current.value?.let { history.addLast(it) }
                _current.value = next
                _prev.value = history.lastOrNull()
                _error.value = null
                if (com.example.wikitok.BuildConfig.DEBUG) {
                    val dt = android.os.SystemClock.elapsedRealtime() - t0
                    android.util.Log.d("Perf", "loadNextInternal dt=${dt}ms")
                    android.util.Log.d("Feed", "show pageId=${next.pageId}")
                }
                viewModelScope.launch { feedBuffer.primeIfNeeded() }
                viewModelScope.launch { updateNextPreview() }
            }
        } catch (t: Throwable) {
            _error.value = t.message ?: "network_error"
        } finally {
            _isFetchingNext.value = false
            _isInitialLoading.value = false
        }
    }

    fun onLike() { viewModelScope.launch { loadNextInternal() } }

    fun onSkip() { viewModelScope.launch { loadNextInternal() } }

    fun refresh() { viewModelScope.launch { loadNextInternal() } }

    fun loadPrev() {
        viewModelScope.launch {
            loadMutex.withLock {
                if (history.isNotEmpty()) {
                    val prev = history.removeLast()
                    _current.value = prev
                    _error.value = null
                    // на всякий случай подпинаем подкачку
                    viewModelScope.launch { feedBuffer.primeIfNeeded() }
                    _prev.value = history.lastOrNull()
                    viewModelScope.launch { updateNextPreview() }
                } else {
                    // если истории нет — оставляем как есть
                    android.util.Log.d("Feed", "prev: history is empty")
                }
            }
        }
    }

    private suspend fun updateNextPreview() {
        runCatching {
            feedBuffer.primeIfNeeded()
            _nextPreview.value = feedBuffer.peekNext()
        }
    }

    // Публичный доступ к следующей статье напрямую из буфера
    suspend fun nextFromBuffer(): Article? = feedBuffer.next()
}
