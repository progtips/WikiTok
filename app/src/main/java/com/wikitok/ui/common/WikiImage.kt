package com.wikitok.ui.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun WikiImage(
    url: String,
    modifier: Modifier = Modifier,
    minHeightDp: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp(160f),
    panoramaHeightDp: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp(220f),
) {
    val context = LocalContext.current

    val imageLoader = remember { WikimediaImageLoader.create(context) }

    val request = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .addHeader("User-Agent", "WikiTok/1.0 (contact: dev@example.com)")
        .addHeader("Referer", "https://wikipedia.org/")
        .listener(
            onStart = { Log.d("WikiImage", "start url=$url") },
            onSuccess = { _, _ -> Log.d("WikiImage", "success url=$url") },
            onError = { _, result -> Log.e("WikiImage", "error url=$url", result.throwable) }
        )
        .build()

    SubcomposeAsyncImage(
        imageLoader = imageLoader,
        model = request,
        contentDescription = null,
        loading = { Box(Modifier.fillMaxWidth().heightIn(min = minHeightDp)) },
        error   = { Box(Modifier.fillMaxWidth().heightIn(min = minHeightDp)) },
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
                    val naturalHeightPx = screenWidthPx / aspect
                    naturalHeightPx.toDp()
                }
                val targetHeight = if (naturalHeightDp < panoramaHeightDp) panoramaHeightDp else naturalHeightDp
                val finalHeight = if (targetHeight < minHeightDp) minHeightDp else targetHeight

                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(finalHeight)
                ) {
                    Image(
                        painter = p,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }
            } else {
                Image(
                    painter = p,
                    contentDescription = null,
                    modifier = modifier
                        .fillMaxWidth()
                        .aspectRatio(clampedAspect)
                        .heightIn(min = minHeightDp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    )
}


