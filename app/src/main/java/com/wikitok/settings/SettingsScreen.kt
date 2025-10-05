package com.wikitok.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLiked: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Тема", style = MaterialTheme.typography.titleMedium)
            SegmentedButtonsTheme(selected = state.theme, onSelect = vm::onThemeChange)

            OutlinedTextField(
                value = state.wikiLang,
                onValueChange = { vm.onWikiLangChange(it.take(5)) },
                label = { Text("Язык Wikipedia (ru / en)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Переключатель сохранения истории скрыт по требованию

            Text("Фон карточек", style = MaterialTheme.typography.titleMedium)
            // 5 цветов: серый (по умолчанию), чёрный, бледно-сиреневый, бледно-жёлтый, фиолетовый
            val palette = listOf(
                "#919191", // серый (по умолчанию)
                "#000000", // чёрный
                "#D8BFD8", // бледно-сиреневый (Thistle)
                "#FFF9C4", // бледно-жёлтый (Yellow 100)
                "#6650A3"  // фиолетовый (текущий фирменный)
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (row in palette.chunked(4)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { hex ->
                            val selected = hex.equals(state.cardBgHex, ignoreCase = true)
                            val color = runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)) }
                                .getOrDefault(MaterialTheme.colorScheme.surface)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selected) 3.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable { vm.onCardBgHexChange(hex) }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onOpenAbout) { Text("О программе") }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Surface(tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedButtonsTheme(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    SingleChoiceSegmentedButtonRow {
        AppTheme.values().forEachIndexed { index, theme ->
            SegmentedButton(
                selected = theme == selected,
                onClick = { onSelect(theme) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTheme.values().size),
                label = { Text(when(theme){ AppTheme.SYSTEM->"Системная"; AppTheme.LIGHT->"Светлая"; AppTheme.DARK->"Тёмная" }) }
            )
        }
    }
}


