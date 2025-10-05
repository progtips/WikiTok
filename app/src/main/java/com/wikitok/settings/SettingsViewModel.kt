package com.wikitok.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val customTabs: Boolean = true,
    val wikiLang: String = "ru",
    val autoScroll: Boolean = false,
    val saveHistory: Boolean = true,
    val cardBgHex: String = "#919191",
    val explorationEpsilon: Float = 0.2f
)

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        repo.settingsFlow.map {
            SettingsUiState(
                theme = it.theme,
                customTabs = it.customTabs,
                wikiLang = it.wikiLang,
                autoScroll = it.autoScroll,
                    saveHistory = it.saveHistory,
                    cardBgHex = it.cardBgHex,
                    explorationEpsilon = it.explorationEpsilon
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onThemeChange(v: AppTheme) = viewModelScope.launch {
        repo.setTheme(v)
        // При смене темы настраиваем фон карточек по требованию
        val hex = when (v) {
            AppTheme.SYSTEM -> "#919191" // серый
            AppTheme.LIGHT -> "#FFF9C4" // бледно-жёлтый
            AppTheme.DARK  -> "#000000" // чёрный
        }
        repo.setCardBgHex(hex)
    }
    fun onCustomTabsChange(v: Boolean) = viewModelScope.launch { repo.setCustomTabs(v) }
    fun onWikiLangChange(v: String) = viewModelScope.launch { repo.setWikiLang(v) }
    fun onAutoScrollChange(v: Boolean) = viewModelScope.launch { repo.setAutoScroll(v) }
    fun onSaveHistoryChange(v: Boolean) = viewModelScope.launch { repo.setSaveHistory(v) }
    fun onCardBgHexChange(v: String) = viewModelScope.launch { repo.setCardBgHex(v) }
    fun onExplorationEpsilonChange(v: Float) = viewModelScope.launch { repo.setExplorationEpsilon(v) }
    fun clearCache() = viewModelScope.launch { repo.clearCache() }
}


