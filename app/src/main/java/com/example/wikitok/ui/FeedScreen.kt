package com.example.wikitok.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCardPlaceholder(index: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "WikiTok — заглушка #${index + 1}",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen() {
    val vm: FeedViewModel = viewModel()
    val items by vm.items.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { max(items.size, 1) })

    LaunchedEffect(pagerState.currentPage, items.size) {
        if (items.size >= 2 && pagerState.currentPage >= items.size - 2) {
            vm.onNeedMore()
        }
    }

    // Учёт времени просмотра страниц
    var lastPage by remember { mutableStateOf<Int?>(null) }
    var lastStartMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(pagerState.currentPage) {
        val now = System.currentTimeMillis()
        val prevPage = lastPage
        if (prevPage != null && lastStartMs > 0L) {
            val prevId = items.getOrNull(prevPage)?.id
            val dwellMs = now - lastStartMs
            if (prevId != null && dwellMs > 0L) {
                vm.onImpression(prevId, dwellMs)
            }
        }
        lastPage = pagerState.currentPage
        lastStartMs = now
    }

    val scope = rememberCoroutineScope()
    VerticalPager(state = pagerState) { page ->
        val article = items.getOrNull(page)
        if (article == null) {
            ArticleCardPlaceholder(page)
        } else {
            ArticleCard(
                a = article,
                onLike = {
                    vm.onLike(article)
                    scope.launch {
                        val next = (page + 1).coerceAtMost(max(items.size - 1, 0))
                        if (next != page) pagerState.animateScrollToPage(next)
                    }
                },
                onDislike = {
                    vm.onDislike(article)
                    scope.launch {
                        val next = (page + 1).coerceAtMost(max(items.size - 1, 0))
                        if (next != page) pagerState.animateScrollToPage(next)
                    }
                }
            )
        }
    }
}


// старый ArticleCard (текстовый) удалён, используем компонент из ArticleCard.kt


