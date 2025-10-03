package com.example.wikitok.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import android.content.Intent
import android.net.Uri
import com.wikitok.settings.LocalSettingsRepository
import com.wikitok.settings.Settings
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import kotlin.math.max
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FeedTopBar(navController: androidx.navigation.NavController) {
    TopAppBar(
        title = { Text("WikiTok") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Настройки"
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCardPlaceholder(index: Int, isLoading: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isLoading) "Загружаем статьи…" else "Нет данных",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(navController: androidx.navigation.NavHostController) {
    val vm: FeedViewModel = viewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.attach(context) }
    val items by vm.items.collectAsState()
    val settingsRepo = LocalSettingsRepository.current
    val settings by settingsRepo.settingsFlow.collectAsState(initial = Settings())

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { max(items.size, 1) })

    // Первичный автозапуск, если по какой-то причине init не успел
    LaunchedEffect(items.size) {
        if (items.isEmpty()) {
            vm.onNeedMore()
        }
    }

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
    Scaffold(
    ) { _ ->
        VerticalPager(state = pagerState) { page ->
            val article = items.getOrNull(page)
            if (article == null) {
                ArticleCardPlaceholder(page, isLoading = items.isEmpty())
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
                    },
                    onOpen = { openArticle(context, article.url, settings) },
                    onOpenSettings = { navController.navigate("settings") }
                )
            }
        }
    }
}

private fun openArticle(context: android.content.Context, url: String, settings: com.wikitok.settings.Settings) {
    if (settings.customTabs) {
        val intent = androidx.browser.customtabs.CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.launchUrl(context, Uri.parse(url))
    } else {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}


// старый ArticleCard (текстовый) удалён, используем компонент из ArticleCard.kt


