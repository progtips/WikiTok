package com.example.wikitok.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.wikitok.settings.LocalSettingsRepository
import com.wikitok.settings.settingsRepository

@Composable
fun AppRoot() {
    val app = LocalContext.current.applicationContext as Application
    val repo = remember { settingsRepository(app) }
    val navController = rememberNavController()
    CompositionLocalProvider(LocalSettingsRepository provides repo) {
        AppNavHost(navController = navController)
    }
}


