package com.example.wikitok.util

import kotlinx.serialization.json.Json

object Jsons {
    val default = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }
}


