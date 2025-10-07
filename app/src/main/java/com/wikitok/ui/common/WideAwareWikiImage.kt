package com.wikitok.ui.common

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.compose.LocalImageLoader
import coil.request.ImageRequest
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette

@Composable
fun WideAwareWikiImage(
    url: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    wideThreshold: Float = 2.2f,
    minSectionHeight: Dp = 140.dp,
    fallbackAspectRatio: Float = 16f/9f
) {
    val context = LocalContext.current
    val imageLoader = LocalImageLoader.current
    val request = remember(url) {
        ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(url)
            .diskCacheKey(url)
            .crossfade(false)
            .allowHardware(false)
            .build()
    }
    val painter = rememberAsyncImagePainter(
        model = request,
        imageLoader = imageLoader
    )

    val intrinsic = painter.intrinsicSize
    val ratio = remember(intrinsic) {
        val w = intrinsic.width
        val h = intrinsic.height
        if (w.isFinite() && h.isFinite() && h > 0f) w / h else fallbackAspectRatio
    }
    val isWide = ratio >= wideThreshold

    var dominant by remember { mutableStateOf<Color?>(null) }
    val fallbackColor = MaterialTheme.colorScheme.surfaceVariant
    LaunchedEffect(painter.state, fallbackColor) {
        val s = painter.state
        if (s is AsyncImagePainter.State.Success) {
            runCatching {
                val bmp = s.result.drawable.toBitmap()
                val p = Palette.from(bmp).clearFilters().generate()
                val dc = p.getDominantColor(fallbackColor.toArgb())
                dominant = Color(dc)
            }
        }
    }

    val container = modifier
        .fillMaxWidth()
        .clip(shape)

    if (!isWide) {
        Box(container) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio),
                contentScale = ContentScale.FillBounds
            )
        }
    } else {
        Box(
            container
                .heightIn(min = minSectionHeight)
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(dominant ?: MaterialTheme.colorScheme.surfaceVariant)
            )

            Box(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}


