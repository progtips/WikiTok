package com.example.wikitok.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.animateFloatAsState
import android.content.Intent
import android.net.Uri
import kotlin.math.max
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wikitok.domain.feed.FeedViewModel as NewFeedVm
import androidx.compose.runtime.snapshotFlow

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
            // Кнопка перехода на "Понравившиеся" убрана из топ-бара; переход доступен через настройки
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCardPlaceholder(index: Int, isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // <-- фон экрана
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isLoading) "Загружаем статьи…" else "Нет данных",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(navController: androidx.navigation.NavHostController) {
    val viewModel: NewFeedVm = hiltViewModel()
    val current by viewModel.current.collectAsState()
    val prev by viewModel.prev.collectAsState()
    val nextPreview by viewModel.nextPreview.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val isFetchingNext by viewModel.isFetchingNext.collectAsState()
    val error by viewModel.error.collectAsState(null)

    val context = LocalContext.current
    val settingsRepo = com.wikitok.settings.LocalSettingsRepository.current
    val settings by settingsRepo.settingsFlow.collectAsState(initial = com.wikitok.settings.Settings())

    val cardBgInt = runCatching {
        android.graphics.Color.parseColor(settings.cardBgHex)
    }.getOrDefault(0xFF919191.toInt())
    val cardBg = Color(cardBgInt)

    Scaffold(containerColor = cardBg) { _ ->
        Box(
            Modifier
                .fillMaxSize()
                .background(cardBg)
        ) {
            when {
                isInitialLoading && current == null -> {
                    androidx.compose.material3.CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                current != null -> {
                    val article = current!!
                    val disableActions = isFetchingNext || isInitialLoading
                    val uiArticle = com.example.wikitok.data.Article(
                        id = article.title,
                        title = article.title,
                        description = article.summary,
                        extract = article.summary,
                        imageUrl = article.imageUrl,
                        url = ""
                    )

                    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
                            .collect { pair ->
                                val scrolling = pair.first
                                val page = pair.second
                                if (scrolling || disableActions) return@collect
                                when (page) {
                                    0 -> { viewModel.loadPrev(); scope.launch { pagerState.scrollToPage(1) } }
                                    2 -> { viewModel.loadNext(); scope.launch { pagerState.scrollToPage(1) } }
                                }
                            }
                    }

                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardBg),
                        userScrollEnabled = !disableActions
                    ) { page ->
                        val aDomain = when (page) {
                            0 -> prev ?: article
                            1 -> article
                            else -> nextPreview ?: article
                        }
                        val ui = com.example.wikitok.data.Article(
                            id = aDomain.title,
                            title = aDomain.title,
                            description = aDomain.summary,
                            extract = aDomain.summary,
                            imageUrl = aDomain.imageUrl,
                            url = ""
                        )
                        ArticleCard(
                            a = ui,
                            onLike = { if (!disableActions) viewModel.onLike() },
                            onDislike = { if (!disableActions) viewModel.onSkip() },
                            onOpen = {
                                val encoded = try {
                                    java.net.URLEncoder.encode(aDomain.title, Charsets.UTF_8.name()).replace('+', '_')
                                } catch (_: Throwable) { aDomain.title.replace(' ', '_') }
                                val lang = java.util.Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ru"
                                val url = "https://${lang}.wikipedia.org/wiki/${encoded}"
                                openArticle(context, url, settings)
                            },
                            onOpenSettings = { navController.navigate("settings") }
                        )
                    }

                    if (isFetchingNext) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                else -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = error ?: "nothing_to_show")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadNext() }) { Text("Повторить") }
                    }
                }
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
