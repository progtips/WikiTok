package com.example.wikitok.domain.feed

import com.example.wikitok.domain.Article

interface IFeedBuffer {
    suspend fun primeIfNeeded()
    suspend fun next(): Article?
}
