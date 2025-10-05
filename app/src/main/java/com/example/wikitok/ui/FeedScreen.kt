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
    val vm: NewFeedVm = hiltViewModel()
    val context = LocalContext.current
    val current by vm.current.collectAsState()
    val loading by vm.loading.collectAsState()
    val isLiked by vm.isLikedCurrent.collectAsState()
    val error by vm.error.collectAsState(null)
    var progress by remember { mutableStateOf(0) }
    val settingsRepo = com.wikitok.settings.LocalSettingsRepository.current
    val settings by settingsRepo.settingsFlow.collectAsState(initial = com.wikitok.settings.Settings())
    val cardBgInt = runCatching { android.graphics.Color.parseColor(settings.cardBgHex) }.getOrDefault(0xFF919191.toInt())
    val cardBgColor = Color(cardBgInt)
    val overlayTextColor = if (settings.cardBgHex.equals("#FFF9C4", ignoreCase = true)) Color.Black else Color.White

    // Делаем 2 страницы: текущая и «следующая», чтобы пользователь мог свайпнуть вверх
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { if (current != null) 2 else 1 })

    // Первичная загрузка текущей статьи
    LaunchedEffect(Unit) { vm.loadNext() }

    // Процент загрузки на стартовом экране (индикативный)
    LaunchedEffect(loading) {
        if (loading) {
            progress = 0
            while (loading && progress < 95) {
                kotlinx.coroutines.delay(120)
                progress = (progress + 3).coerceAtMost(95)
            }
        } else if (current == null) {
            // если загрузка закончилась, но данных нет
            progress = 100
        }
    }

    // Если пользователь доскроллил до второй страницы — подгружаем следующую один раз за визит на страницу 1
    var requestedNext by remember { mutableStateOf(false) }
    // Если пользователь доскроллил до страницы 1 жестом — один раз запросим следующую
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 1 && !requestedNext) {
            requestedNext = true
            vm.loadNext()
        }
    }
    // Когда текущая карточка сменилась после запроса next — плавно вернёмся на страницу 0 (если не идёт жест)
    LaunchedEffect(current) {
        // Если текущая карточка пропала (например, во время подгрузки) — гарантированно вернёмся на страницу 0,
        // чтобы пейджер не завис на пустой второй странице
        if (current == null && pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
        if (requestedNext) {
            // подождём, чтобы композиция успела обновить содержимое
            kotlinx.coroutines.delay(120)
            if (pagerState.currentPage == 1 && !pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(0)
            }
            requestedNext = false
        }
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        // Задаём фон всего экрана из темы
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
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
                val showLoadingNext = requestedNext
                if (page == 1) {
                    // Заглушка «следующая карточка»
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardBgColor),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (showLoadingNext) "Загружаем следующую…" else "Проведите вверх для следующей статьи",
                            color = overlayTextColor
                        )
                    }
                } else if (article == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (loading) {
                            Text("Загружаем статьи")
                            Spacer(Modifier.height(8.dp))
                            Text("${progress}%")
                        } else {
                            val message = if (error != null) "Проблема с сетью" else "Нет данных"
                            Text(message)
                        }
                    }
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
                            // Запрашиваем следующую карточку и визуально переводим на страницу 1
                            requestedNext = true
                            vm.onLike()
                            scope.launch {
                                if (pagerState.currentPage == 0 && current != null && !pagerState.isScrollInProgress) {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                            scope.launch {
                                kotlinx.coroutines.delay(180)
                                likePulse = false
                            }
                        },
                        onDislike = {
                            // Поведение, аналогичное лайку: анимируем на страницу 1 и запрашиваем следующую
                            requestedNext = true
                            vm.onSkip()
                            scope.launch {
                                if (pagerState.currentPage == 0 && current != null && !pagerState.isScrollInProgress) {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        },
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

            // Нижняя панель кнопок убрана: используем существующие кнопки в карточке
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
