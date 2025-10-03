package com.wikitok.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.MaterialTheme
import coil.compose.AsyncImagePainter
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.compose.AsyncImage
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log
import androidx.compose.runtime.LaunchedEffect

@Composable
fun WikiImage(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = java.util.Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ru"

    // Единый ImageLoader с поддержкой SVG и нужными заголовками
    val imageLoader = remember(context, lang) {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val headers = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .header("User-Agent", "WikiTok/1.0 (https://wikitok.app; dev@wikitok.app)")
                .header("Accept-Language", lang)
                .build()
            chain.proceed(req)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(headers)
            .addInterceptor(logger)
            .build()
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .okHttpClient(client)
            .crossfade(true)
            .build()
    }

    val model = ImageRequest.Builder(context)
        .data(url)
        .build()

    // Используем painter, чтобы безопасно вычислить аспект по intrinsic size
    val painter = rememberAsyncImagePainter(model = model, imageLoader = imageLoader)

    val aspect = remember(painter) {
        val s = painter.intrinsicSize
        if (s != Size.Unspecified) {
            val h = s.height
            val w = s.width
            if (h > 0f && w.isFinite() && h.isFinite()) w / h else 16f / 9f
        } else 16f / 9f
    }

    val imageModifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceVariant)

    // Адаптивная стратегия визуализации:
    // - Если изображение широкое (aspect >= 1.3) — используем Crop и фиксируем высоту по ширине (aspectRatio)
    // - Иначе — Fit без обрезки
    val wideThreshold = 1.3f
    val aspectOrDefault = aspect.coerceIn(0.5f, 3.5f)
    val isWide = aspectOrDefault >= wideThreshold

    // Лог состояний
    LaunchedEffect(painter.state) {
        Log.d("WikiImage", "state=${painter.state} url=${url} aspect=${aspect}")
    }

    val visualModifier = if (isWide) imageModifier.aspectRatio(aspectOrDefault) else imageModifier

    when (painter.state) {
        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = visualModifier,
                contentScale = if (isWide) ContentScale.Crop else ContentScale.Fit
            )
        }
        is AsyncImagePainter.State.Error -> {
            val t = (painter.state as AsyncImagePainter.State.Error).result.throwable
            Log.e("WikiImage", "Image load error for url=$url", t)
            Box(visualModifier) { }
        }
        else -> {
            Box(visualModifier) { }
        }
    }
}


