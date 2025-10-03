package com.wikitok.ui.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun WikiImage(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = remember { WikimediaImageLoader.create(context) }

    val request = ImageRequest.Builder(context)
        .data(url)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
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
        loading = {
            Box(Modifier.fillMaxWidth().aspectRatio(1f))
        },
        error = {
            Box(Modifier.fillMaxWidth().aspectRatio(1f))
        },
        success = { state ->
            val p = state.painter
            val s = p.intrinsicSize
            val aspect = if (s != Size.Unspecified && s.height > 0f) s.width / s.height else 1f

            Image(
                painter = p,
                contentDescription = null,
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect),
                contentScale = ContentScale.FillWidth
            )
        }
    )
}


