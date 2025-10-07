package com.example.wikitok

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WikiTokApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(false)
            .components {
                // Поддержка SVG (безопасно, если зависимость подключена)
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
