package com.example.wikitok.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wikitok.BuildConfig
import com.example.wikitok.R

/**
 * Экран «О программе» для WikiTok (Jetpack Compose, Material 3).
 * - Показывает логотип, название, версию
 * - Краткое и расширенное описание
 * - Ссылки: Политика конфиденциальности, Открытые лицензии, Связаться
 * - Кнопка «Назад» (опционально)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val appName = context.getString(R.string.app_name)
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    val openUrl: (String) -> Unit = remember {
        { url ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) { /* no-op */ }
        }
    }

    val sendEmail: () -> Unit = remember {
        {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@wikitok.app"))
                putExtra(Intent.EXTRA_SUBJECT, "WikiTok — Обратная связь")
            }
            try { context.startActivity(intent) } catch (_: ActivityNotFoundException) { /* no-op */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "О программе") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Логотип приложения
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(Modifier.padding(20.dp)) {
                    val iconPainter =
                        runCatching { painterResource(id = R.drawable.ic_launcher_foreground) }.getOrNull()
                            ?: runCatching { painterResource(id = R.mipmap.ic_launcher_round) }.getOrNull()

                    if (iconPainter != null) {
                        Image(
                            painter = iconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = appName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Версия $versionName ($versionCode)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Краткое описание
            Text(
                text = "WikiTok — приложение, которое показывает случайные статьи из Википедии в формате коротких карточек, похожем на TikTok.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Расширенное описание — обрезано по требованию до фразы «в один тап.»
            Text(
                text = "Листайте, чтобы узнавать новое каждый день: факты, события, людей и явления из разных областей знаний. Отмечайте понравившиеся статьи, и приложение будет лучше подстраивать ленту под ваши интересы. Переходите к полным статьям в браузере в один тап.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Карточки со ссылками/действиями
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    AboutItemRow(
                        icon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                        title = "Оценить приложение",
                        subtitle = "Откроется страница магазина",
                        onClick = { openUrl("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}") }
                    )
                    Divider()
                    AboutItemRow(
                        icon = { Icon(Icons.Outlined.PrivacyTip, contentDescription = null) },
                        title = "Политика конфиденциальности",
                        subtitle = "Как мы работаем с данными",
                        onClick = { openUrl("https://wikitok.app/privacy") }
                    )
                    
                    Divider()
                    AboutItemRow(
                        icon = { Icon(Icons.Outlined.OpenInNew, contentDescription = null) },
                        title = "Источник данных: Wikipedia.org",
                        subtitle = "Перейти на сайт Википедии",
                        onClick = { openUrl("https://wikipedia.org") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Контакты
            Text(
                text = "Нужна помощь или есть идея?",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(onClick = sendEmail, shape = RoundedCornerShape(14.dp)) {
                Text("Написать в поддержку")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Футер
            Text(
                text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} WikiTok Team",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AboutItemRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle != null) Text(subtitle) },
        leadingContent = icon,
        trailingContent = { Icon(Icons.Outlined.OpenInNew, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 4.dp)
            .let { base -> base }
            .clickable(onClick = onClick)
    )
}

// Превью и устаревший ripple удалены
