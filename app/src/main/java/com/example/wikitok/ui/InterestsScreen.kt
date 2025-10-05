package com.example.wikitok.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(onBack: () -> Unit, vm: InterestsViewModel = hiltViewModel()) {
    val weights by vm.weights.collectAsState()
    val top by vm.top.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои интересы") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { vm.resetAll() }) { Text("Сбросить") }
            }

            top.forEach { cat ->
                val value = weights[cat] ?: 0f
                Text(cat)
                Slider(
                    value = value,
                    onValueChange = { vm.setWeight(cat, it.coerceIn(-1f, 1f)) },
                    valueRange = -1f..1f,
                    steps = 10
                )
                Text("${"%.2f".format(value)}")
                Divider()
            }
        }
    }
}


