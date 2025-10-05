package com.example.wikitok.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LikedScreen(
    onBack: () -> Unit,
    vm: LikedViewModel = hiltViewModel()
) {
    val items by vm.liked.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Понравившиеся") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad)) {
            items(items) { a ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { pendingDelete = a.pageId }
                        )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(a.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (a.categories.isNotEmpty()) Text(a.categories.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Лайк: неизвестно", style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = {
                                val encoded = try { java.net.URLEncoder.encode(a.title, Charsets.UTF_8.name()).replace('+', '_') } catch (_: Throwable) { a.title.replace(' ', '_') }
                                val lang = java.util.Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ru"
                                val url = "https://${lang}.wikipedia.org/wiki/${encoded}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }) { Text("Открыть в Википедии") }
                        }
                    }
                }
            }
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Удалить из понравившихся?") },
            confirmButton = {
                TextButton(onClick = {
                    val id = pendingDelete!!
                    scope.launch { vm.unlike(id) }
                    pendingDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Отмена") } }
        )
    }
}
