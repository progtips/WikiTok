package com.example.wikitok.domain.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.Article
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val likesRepository: ILikesRepository,
    private val preferencesStore: ICategoryWeightsStore,
    private val feedBuffer: IFeedBuffer
) : ViewModel() {

    private val _current = MutableStateFlow<Article?>(null)
    val current: StateFlow<Article?> = _current

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _isFetchingNext = MutableStateFlow(false)
    val isFetchingNext: StateFlow<Boolean> = _isFetchingNext

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    @OptIn(ExperimentalCoroutinesApi::class)
    val isLikedCurrent: StateFlow<Boolean> = _current
        .flatMapLatest { a ->
            val id = a?.pageId ?: -1L
            if (id <= 0) flowOf(false) else likesRepository.isLiked(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val loadMutex = Mutex()

    init {
        viewModelScope.launch { loadNextInternal() }
    }

    fun loadNext() {
        viewModelScope.launch { loadNextInternal() }
    }

    private suspend fun loadNextInternal() {
        // 1) Пинаем подкачку (быстро возвращается)
        runCatching { feedBuffer.primeIfNeeded() }

        val firstLoad = _current.value == null
        if (firstLoad) _isInitialLoading.value = true else _isFetchingNext.value = true

        try {
            var next: Article? = null
            repeat(3) { attempt ->
                val candidate = runCatching { feedBuffer.next() }.getOrNull()
                if (candidate != null && candidate.pageId != _current.value?.pageId) {
                    next = candidate
                    return@repeat
                }
                // уменьшенная задержка, чтобы ускорить перелистывание
                kotlinx.coroutines.delay(60L + attempt * 60L)
                runCatching { feedBuffer.primeIfNeeded() }
            }

            if (next == null) {
                _error.value = "empty_feed"
                return
            }

            _current.value = next
            _error.value = null
            if (com.example.wikitok.BuildConfig.DEBUG) {
                android.util.Log.d("Feed", "show pageId=" + next?.pageId)
            }
        } catch (t: Throwable) {
            _error.value = t.message ?: "network_error"
        } finally {
            _isFetchingNext.value = false
            _isInitialLoading.value = false
        }
    }

    fun onLike() {
        val a = _current.value ?: return
        viewModelScope.launch {
            if (com.example.wikitok.BuildConfig.DEBUG) {
                android.util.Log.d("Feed", "like pageId=" + a.pageId + ", cats=" + a.categories)
            }
            likesRepository.like(a)
            preferencesStore.bumpPositive(a.categories)
            loadNextInternal()
        }
    }

    fun onSkip() {
        val a = _current.value ?: return
        viewModelScope.launch {
            if (com.example.wikitok.BuildConfig.DEBUG) {
                android.util.Log.d("Feed", "skip pageId=" + a.pageId + ", cats=" + a.categories)
            }
            preferencesStore.bumpNegative(a.categories)
            loadNextInternal()
        }
    }

    fun unlike(pageId: Long) {
        viewModelScope.launch { likesRepository.unlike(pageId) }
    }
}
