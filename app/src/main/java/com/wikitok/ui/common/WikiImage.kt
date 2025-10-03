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
        .crossfade(true)
        .addHeader("User-Agent", "WikiTok/1.0 (contact: dev@example.com)")
        .addHeader("Referer", "https://wikipedia.org/")
        .build()

    SubcomposeAsyncImage(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        imageLoader = imageLoader,
        model = request,
        contentDescription = null,

        // Плейсхолдеры: та же подложка, что и у экрана
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeightDp)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {}
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeightDp)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {}
        },

        success = { state ->
            val p = state.painter
            val s = p.intrinsicSize
            val aspect = if (s != Size.Unspecified && s.height > 0f) s.width / s.height else 1f

            val isPanorama = aspect > 2.0f
            val clampedAspect = aspect.coerceIn(0.6f, 1.8f)

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
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    // Заполняем весь контейнер, чтобы не оставалось просветов
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.background)
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
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                val screenWidthDp = configuration.screenWidthDp.dp
                val naturalHeightDp: Dp = with(density) {
                    val screenWidthPx = screenWidthDp.toPx()
                    (screenWidthPx / clampedAspect).toDp()
                }

                val targetHeight = if (naturalHeightDp < minHeightDp) minHeightDp else naturalHeightDp

                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(targetHeight)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    // Заполняем весь контейнер и обрезаем лишнее — без белых полей
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.background)
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
        }
    )
}
