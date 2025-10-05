package com.wikitok.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wikitok.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О программе") },
                navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("WikiTok", style = MaterialTheme.typography.titleLarge)
            Text("Версия: " + BuildConfig.VERSION_NAME)
            Spacer(Modifier.height(8.dp))
            Text("О приложении", style = MaterialTheme.typography.titleMedium)
            Text("Лента карточек из Википедии с умной рекомендацией: учитываем категории, веса интересов и ε‑разнообразие.")
            Spacer(Modifier.height(8.dp))
            Text("Возможности", style = MaterialTheme.typography.titleMedium)
            Text("• Лайк/пропуск")
            Text("• Экран ‘Понравившиеся’ и открытие статьи в Custom Tabs")
            Text("• Настройки: язык, фон карточек, мои интересы (ползунки)")
            Text("• История недавних показов, чтобы не повторять статьи")
            Spacer(Modifier.height(8.dp))
            Text("Источник данных", style = MaterialTheme.typography.titleMedium)
            Text("Wikipedia REST API. Приложение не связано с Wikimedia Foundation.")
        }
    }
}


