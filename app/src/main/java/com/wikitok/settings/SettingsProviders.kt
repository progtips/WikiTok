package com.wikitok.settings

import android.app.Application
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

val LocalSettingsRepository = staticCompositionLocalOf<SettingsRepository> {
    error("SettingsRepository not provided")
}

class SettingsVmFactory(private val repo: SettingsRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(c: Class<T>): T {
        if (c.isAssignableFrom(SettingsViewModel::class.java)) return SettingsViewModel(repo) as T
        error("Unknown ViewModel class")
    }
}

fun settingsRepository(app: Application) = SettingsRepository(app)


