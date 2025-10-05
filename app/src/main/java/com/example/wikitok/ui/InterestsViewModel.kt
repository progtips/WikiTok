package com.example.wikitok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikitok.data.prefs.ICategoryWeightsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class InterestsViewModel @Inject constructor(
    private val store: ICategoryWeightsStore
) : ViewModel() {
    val weights: StateFlow<Map<String, Float>> = store.observeWeights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val top: StateFlow<List<String>> = store.observeWeights().map { map ->
        map.entries.sortedByDescending { it.value }.map { it.key }.take(20)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setWeight(category: String, value: Float) = viewModelScope.launch {
        store.setWeight(category, value)
    }

    fun resetAll() = viewModelScope.launch { store.resetAll() }
}


