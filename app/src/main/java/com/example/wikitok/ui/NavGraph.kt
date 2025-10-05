package com.example.wikitok.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.wikitok.settings.LocalSettingsRepository
import com.wikitok.settings.SettingsScreen
import com.wikitok.settings.SettingsVmFactory
import com.wikitok.settings.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wikitok.settings.AboutScreen

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = "feed") {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("feed") {
            FeedScreen(navController)
        }
        composable("settings") {
            val repo = LocalSettingsRepository.current
            val vm = viewModel<SettingsViewModel>(factory = SettingsVmFactory(repo))
            SettingsScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate("about") },
                onOpenLiked = { navController.navigate("liked") }
            )
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable("liked") {
            LikedScreen(onBack = { navController.popBackStack() })
        }
        composable("interests") {
            InterestsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "webview?url={url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("url").orEmpty()
            val decoded = try { java.net.URLDecoder.decode(raw, Charsets.UTF_8.name()) } catch (_: Throwable) { raw }
            WebViewScreen(url = decoded, modifier = Modifier.fillMaxSize())
        }
    }
}


