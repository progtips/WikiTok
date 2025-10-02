package com.wikitok.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wikitok.BuildConfig

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О программе") },
                navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("WikiTok", style = MaterialTheme.typography.titleLarge)
            Text("Версия: " + BuildConfig.VERSION_NAME)
            Text("Случайные статьи Википедии в формате карточек.")
        }
    }
}


