package com.wikitok.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    onOpenAbout: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Тема", style = MaterialTheme.typography.titleMedium)
            SegmentedButtonsTheme(selected = state.theme, onSelect = vm::onThemeChange)

            SettingSwitch("Открывать в Custom Tabs", state.customTabs, vm::onCustomTabsChange, "Рекомендовано")

            OutlinedTextField(
                value = state.wikiLang,
                onValueChange = { vm.onWikiLangChange(it.take(5)) },
                label = { Text("Язык Wikipedia (ru / en)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            SettingSwitch("Автопрокрутка карточек", state.autoScroll, vm::onAutoScrollChange)
            SettingSwitch("Сохранять историю просмотров", state.saveHistory, vm::onSaveHistoryChange)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { vm.clearCache() }) { Text("Очистить кэш") }
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


