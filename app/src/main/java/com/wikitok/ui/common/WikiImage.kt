package com.wikitok.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wikitok.settings.LocalSettingsRepository

@Composable
fun WikiImage(
    url: String,
    modifier: Modifier = Modifier,
    minHeightDp: Dp = 300.dp,
    panoramaHeightDp: Dp = 300.dp,
) {
    val context = LocalContext.current
    val imageLoader = remember { WikimediaImageLoader.create(context) }

    val request = ImageRequest.Builder(context)
        .data(url)
        .crossfade(false)
        .addHeader("User-Agent", "WikiTok/1.0 (contact: dev@example.com)")
        .addHeader("Referer", "https://wikipedia.org/")
        .build()

    val settingsRepo = LocalSettingsRepository.current
    val settings by settingsRepo.settingsFlow.collectAsState(initial = com.wikitok.settings.Settings())
    val cardBgColor = runCatching { android.graphics.Color.parseColor(settings.cardBgHex) }.getOrDefault(0xFF919191.toInt())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color(cardBgColor)),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier.fillMaxWidth(),
            imageLoader = imageLoader,
            model = request,
            contentDescription = null,

        // Плейсхолдеры: та же подложка, что и у экрана
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeightDp)
                    .background(androidx.compose.ui.graphics.Color(cardBgColor)),
                contentAlignment = Alignment.Center
            ) {}
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeightDp)
                    .background(androidx.compose.ui.graphics.Color(cardBgColor)),
                contentAlignment = Alignment.Center
            ) {}
        },

        success = { state ->
            val p = state.painter
            val s = p.intrinsicSize
            val aspect = if (s != Size.Unspecified && s.height > 0f) s.width / s.height else 1f

            val isPanorama = aspect > 2.0f
            val clampedAspect = aspect.coerceIn(0.4f, 2.0f)

            if (isPanorama) {
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                val screenWidthDp = configuration.screenWidthDp.dp
                val naturalHeightDp: Dp = with(density) {
                    val screenWidthPx = screenWidthDp.toPx()
                    (screenWidthPx / aspect).toDp()
                }

                val targetHeight = if (naturalHeightDp < panoramaHeightDp) panoramaHeightDp else naturalHeightDp
                val finalHeight = if (targetHeight < minHeightDp) minHeightDp else targetHeight

                // «Белое поле» (подложка) = фон экрана
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(finalHeight)
                        .background(androidx.compose.ui.graphics.Color(cardBgColor)),
                    contentAlignment = Alignment.Center
                ) {
                    // Заполняем весь контейнер, чтобы не оставалось просветов
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(androidx.compose.ui.graphics.Color(cardBgColor))
                    ) {
                        Image(
                            painter = p,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    }
                }
            } else {
                // Используем реальный аспект изображения для высоты контейнера — без фиксированных высот
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .aspectRatio(clampedAspect)
                        .background(androidx.compose.ui.graphics.Color(cardBgColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = p,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }
            }
        }
        )
    }
}
