package com.example.wikitok.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
            IconButton(onClick = { navController.navigate("liked") }) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Понравившиеся"
                )
            }
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
    val vm: NewFeedVm = hiltViewModel()
    val context = LocalContext.current
    val current by vm.current.collectAsState()
    val loading by vm.loading.collectAsState()
    val isLiked by vm.isLikedCurrent.collectAsState()
    val settingsRepo = com.wikitok.settings.LocalSettingsRepository.current
    val settings by settingsRepo.settingsFlow.collectAsState(initial = com.wikitok.settings.Settings())

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 1 })

    // Первичная загрузка текущей статьи
    LaunchedEffect(Unit) { vm.loadNext() }

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        // Задаём фон всего экрана из темы
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        var likePulse by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(targetValue = if (likePulse || isLiked) 1.2f else 1.0f, label = "heartScale")
        val haptics = LocalHapticFeedback.current

        Box(Modifier.fillMaxSize()) {
            VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // <-- фон под пейджером
            ) { page ->
                val article = current
                if (article == null) {
                    ArticleCardPlaceholder(page, isLoading = loading)
                } else {
                    val uiArticle = com.example.wikitok.data.Article(
                        id = article.title,
                        title = article.title,
                        description = article.summary,
                        extract = article.summary,
                        imageUrl = article.imageUrl,
                        url = ""
                    )
                    // Карточка
                    ArticleCard(
                        a = uiArticle,
                        onLike = {
                            likePulse = true
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.onLike()
                            scope.launch {
                                kotlinx.coroutines.delay(180)
                                likePulse = false
                                val res = snackbarHostState.showSnackbar(
                                    message = "Добавлено в понравившиеся",
                                    actionLabel = "Отменить",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    val id = current?.pageId ?: return@launch
                                    vm.unlike(id)
                                }
                            }
                        },
                        onDislike = { vm.onSkip() },
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
                    // Анимированное сердечко поверх карточки (индикатор лайка)
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isLiked) "Понравилось" else "Не понравилось",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .scale(scale)
                    )
                }
            }

            // Нижняя панель с большими кнопками
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { vm.onSkip() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6650A3), contentColor = Color.White)
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Пропустить")
                    Spacer(Modifier.width(8.dp))
                    Text("Пропустить")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        likePulse = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.onLike()
                        scope.launch {
                            kotlinx.coroutines.delay(180)
                            likePulse = false
                            val res = snackbarHostState.showSnackbar(
                                message = "Добавлено в понравившиеся",
                                actionLabel = "Отменить",
                                withDismissAction = true,
                                duration = SnackbarDuration.Short
                            )
                            if (res == SnackbarResult.ActionPerformed) {
                                val id = current?.pageId ?: return@launch
                                vm.unlike(id)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6650A3), contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isLiked) "Нравится (выбрано)" else "Нравится"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Нравится")
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
