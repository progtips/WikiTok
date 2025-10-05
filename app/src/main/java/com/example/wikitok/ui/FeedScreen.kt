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
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()
    val isFetchingNext by viewModel.isFetchingNext.collectAsState()
    val error by viewModel.error.collectAsState(null)

    val context = LocalContext.current
    val settingsRepo = com.wikitok.settings.LocalSettingsRepository.current
    val settings by settingsRepo.settingsFlow.collectAsState(initial = com.wikitok.settings.Settings())

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { _ ->
        Box(Modifier.fillMaxSize()) {
            when {
                isInitialLoading && current == null -> {
                    androidx.compose.material3.CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                current != null -> {
                    val article = current!!
                    val uiArticle = com.example.wikitok.data.Article(
                        id = article.title,
                        title = article.title,
                        description = article.summary,
                        extract = article.summary,
                        imageUrl = article.imageUrl,
                        url = ""
                    )
                    ArticleCard(
                        a = uiArticle,
                        onLike = { if (!isFetchingNext) viewModel.onLike() },
                        onDislike = { if (!isFetchingNext) viewModel.onSkip() },
                        onOpen = {
                            val encoded = try {
                                java.net.URLEncoder.encode(article.title, Charsets.UTF_8.name()).replace('+', '_')
                            } catch (_: Throwable) { article.title.replace(' ', '_') }
                            val lang = java.util.Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ru"
                            val url = "https://${lang}.wikipedia.org/wiki/${encoded}"
                            openArticle(context, url, settings)
                        },
                        onOpenSettings = { navController.navigate("settings") }
                    )

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
