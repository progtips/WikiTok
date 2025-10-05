package com.wikitok.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class AppTheme { SYSTEM, LIGHT, DARK }

data class Settings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val customTabs: Boolean = true,
    val wikiLang: String = "ru",
    val autoScroll: Boolean = false,
    val saveHistory: Boolean = true,
    val cardBgHex: String = "#919191",
    val explorationEpsilon: Float = 0.2f
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val CUSTOM_TABS = booleanPreferencesKey("custom_tabs")
        val WIKI_LANG = stringPreferencesKey("wiki_lang")
        val AUTO_SCROLL = booleanPreferencesKey("auto_scroll")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")
        val CARD_BG_HEX = stringPreferencesKey("card_bg_hex")
        val EPSILON = floatPreferencesKey("exploration_epsilon")
    }

    val settingsFlow: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            theme = runCatching { AppTheme.valueOf(p[Keys.THEME] ?: "SYSTEM") }.getOrDefault(AppTheme.SYSTEM),
            customTabs = p[Keys.CUSTOM_TABS] ?: true,
            wikiLang = p[Keys.WIKI_LANG] ?: "ru",
            autoScroll = p[Keys.AUTO_SCROLL] ?: false,
            saveHistory = p[Keys.SAVE_HISTORY] ?: true,
            cardBgHex = p[Keys.CARD_BG_HEX] ?: "#919191",
            explorationEpsilon = p[Keys.EPSILON] ?: 0.2f
        )
    }

    suspend fun setTheme(v: AppTheme) = context.dataStore.edit { it[Keys.THEME] = v.name }
    suspend fun setCustomTabs(v: Boolean) = context.dataStore.edit { it[Keys.CUSTOM_TABS] = v }
    suspend fun setWikiLang(v: String) = context.dataStore.edit { it[Keys.WIKI_LANG] = v.ifBlank { "ru" } }
    suspend fun setAutoScroll(v: Boolean) = context.dataStore.edit { it[Keys.AUTO_SCROLL] = v }
    suspend fun setSaveHistory(v: Boolean) = context.dataStore.edit { it[Keys.SAVE_HISTORY] = v }
    suspend fun setCardBgHex(v: String) = context.dataStore.edit { it[Keys.CARD_BG_HEX] = v }
    suspend fun setExplorationEpsilon(v: Float) = context.dataStore.edit { it[Keys.EPSILON] = v.coerceIn(0f, 1f) }

    suspend fun clearCache() { /* TODO: очистка кэша/БД при необходимости */ }
}


