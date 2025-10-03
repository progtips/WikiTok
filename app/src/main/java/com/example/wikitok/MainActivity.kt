package com.example.wikitok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.wikitok.ui.AppRoot
import com.example.wikitok.ui.theme.WikiTokTheme
import com.example.wikitok.ui.FeedScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ВАЖНО: dynamicColor = false, чтобы система не подсовывала белый фон
            WikiTokTheme(dynamicColor = false) {
                // Двойная страховка: Surface + Box с фоном темы
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        AppRoot()
                    }
                }
            }
        }
    }
}