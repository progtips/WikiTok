package com.wikitok.ui.common

import android.content.Context
import coil.ImageLoader
import okhttp3.OkHttpClient

object WikimediaImageLoader {
    fun create(context: Context): ImageLoader {
        val okHttp = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "WikiTok/1.0 (contact: dev@example.com)")
                    .header("Referer", "https://wikipedia.org/")
                    .build()
                chain.proceed(req)
            }
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(okHttp)
            .build()
    }
}


