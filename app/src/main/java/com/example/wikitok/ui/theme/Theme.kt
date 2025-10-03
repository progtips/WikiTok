package com.example.wikitok.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// OPAQUE палитры без альфы — не будут просвечивать «белый» подложки окна
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFFFF00FF),   // ядрёная маджента для проверки фона
    surface    = Color(0xFF1E1E1E),   // было: Color.Black.copy(alpha = 0.6f)
    onBackground = Color(0xFFECECEC),
    onSurface    = Color(0xFFECECEC)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFF00FF),   // ядрёная маджента для проверки фона
    surface    = Color(0xFFFFFFFF),   // карточки/плашки
    onBackground = Color(0xFF1C1B1F),
    onSurface    = Color(0xFF1C1B1F)
)

@Composable
fun WikiTokTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Если хочешь стабильный серый фон и на Android 12+, отключи dynamicColor = false
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
