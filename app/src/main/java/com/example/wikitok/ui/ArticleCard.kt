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
import com.example.wikitok.data.Article
import com.wikitok.ui.common.WikiImage

@Composable
fun ArticleCard(
    a: Article,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onOpen: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // Подложка карточки = background (серый из темы), чтобы фон за картинкой не был белым
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onOpen() }
    ) {
        // Центрирование по вертикали происходит внутри WikiImage (через внутреннюю рамку aspectRatio)
        a.imageUrl?.let {
            WikiImage(
                url = it,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Нижняя панель с полу-прозрачной плашкой — без изменений
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Text(a.title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            if (!a.description.isNullOrBlank()) Text(a.description!!, color = Color.White)
            if (!a.extract.isNullOrBlank()) Text(a.extract!!, maxLines = 4, color = Color.White)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDislike) { Text("Пропустить", color = Color.White) }
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Настройки",
                    tint = Color.White,
                    modifier = Modifier.clickable { onOpenSettings() }
                )
                Button(onClick = onLike) { Text("Нравится") }
            }
        }
    }
}
