package com.example.wikitok.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.wikitok.data.Article
import com.wikitok.ui.common.WikiImage
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wikitok.settings.LocalSettingsRepository
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun ArticleCard(
    a: Article,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // Подложка карточки: возвращаем фон из темы
    val settingsRepo = LocalSettingsRepository.current
    val settings by settingsRepo.settingsFlow.collectAsState(initial = com.wikitok.settings.Settings())

    val cardBgColor = runCatching {
        android.graphics.Color.parseColor(settings.cardBgHex)
    }.getOrDefault(0xFF919191.toInt())
    val overlayTextColor = if (settings.cardBgHex.equals("#FFF9C4", ignoreCase = true)) Color.Black else Color.White

    val talkBackText = buildString {
        append(a.title)
        val desc = a.description ?: a.extract
        if (!desc.isNullOrBlank()) {
            append(". ")
            append(desc)
        }
    }

    var rawOffsetY by remember { mutableStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(targetValue = rawOffsetY, label = "articleSwipeOffset")
    val offsetY = animatedOffsetY
    var dragged by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = talkBackText }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragged) onNext()
                        rawOffsetY = 0f
                        dragged = false
                    },
                    onDragCancel = { rawOffsetY = 0f; dragged = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        rawOffsetY += dragAmount.y
                        if (!dragged && kotlin.math.abs(rawOffsetY) > 4f) dragged = true
                        if (kotlin.math.abs(rawOffsetY) > 48f) {
                            onNext()
                            rawOffsetY = 0f
                            dragged = false
                            // сбрасываем активный жест
                            change.consume()
                        }
                    }
                )
            }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.roundToInt()) }
        ) {
            // Верхняя область под изображение: фон как у плашки текста (#919191)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(cardBgColor)),
                contentAlignment = Alignment.Center
            ) {
                a.imageUrl?.let {
                    WikiImage(
                        url = it,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Нижняя панель с фиксированной плашкой под текст
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(cardBgColor))
                    .navigationBarsPadding()
                    .padding(12.dp)
            ) {
                Text(
                    a.title,
                    color = overlayTextColor,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                val secondaryText: String? = when {
                    !a.description.isNullOrBlank() -> a.description
                    !a.extract.isNullOrBlank() -> a.extract
                    else -> null
                }
                if (!secondaryText.isNullOrBlank()) {
                    Text(
                        secondaryText,
                        color = overlayTextColor,
                        maxLines = 7,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDislike,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6650A3),
                            contentColor = Color.White
                        )
                    ) { Text("Пропустить") }
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Настройки",
                        tint = overlayTextColor,
                        modifier = Modifier.clickable { onOpenSettings() }
                    )
                    Button(
                        onClick = onLike,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6650A3),
                            contentColor = Color.White
                        )
                    ) { Text("Нравится") }
                }
            }
        }
    }
}
