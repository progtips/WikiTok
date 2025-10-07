package com.example.wikitok.ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.LocalImageLoader

@Composable
fun WikiImage(
    url: String?,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = LocalImageLoader.current
) {
    if (url.isNullOrBlank()) {
        Box(modifier) {}
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = modifier
        )
    }
}


