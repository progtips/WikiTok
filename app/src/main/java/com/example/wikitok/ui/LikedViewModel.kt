package com.example.wikitok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.likes.ILikesRepository
import com.example.wikitok.domain.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class LikedViewModel @Inject constructor(
    private val likesRepository: ILikesRepository
) : ViewModel() {
    val liked: StateFlow<List<Article>> = likesRepository
        .likedFeed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun unlike(id: Long) {
        likesRepository.unlike(id)
    }
}
