package com.example.wikitok.domain.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import com.example.wikitok.domain.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val likesRepository: ILikesRepository,
    private val preferencesStore: ICategoryWeightsStore,
    private val feedBuffer: IFeedBuffer
) : ViewModel() {

    private val _current = MutableStateFlow<Article?>(null)
    val current: StateFlow<Article?> = _current

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    @OptIn(ExperimentalCoroutinesApi::class)
    val isLikedCurrent: StateFlow<Boolean> = _current
        .flatMapLatest { a ->
            val id = a?.pageId ?: -1L
            if (id <= 0) flowOf(false) else likesRepository.isLiked(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var loadJob: Job? = null

    fun loadNext() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _loading.value = true
            try {
                feedBuffer.primeIfNeeded()
                val next = feedBuffer.next()
                _current.value = next
            } finally {
                _loading.value = false
            }
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
            loadNext()
        }
    }

    fun onSkip() {
        val a = _current.value ?: return
        viewModelScope.launch {
            if (com.example.wikitok.BuildConfig.DEBUG) {
                android.util.Log.d("Feed", "skip pageId=" + a.pageId + ", cats=" + a.categories)
            }
            preferencesStore.bumpNegative(a.categories)
            loadNext()
        }
    }

    fun unlike(pageId: Long) {
        viewModelScope.launch {
            likesRepository.unlike(pageId)
        }
    }
}
