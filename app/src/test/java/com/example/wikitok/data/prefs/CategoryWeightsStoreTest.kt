package com.example.wikitok.data.prefs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryWeightsStoreTest {

    @Test
    fun bump_and_clamp() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = CategoryWeightsStore(ctx)
        store.bumpPositive(listOf("a"), delta = 0.9f)
        store.bumpPositive(listOf("a"), delta = 0.9f) // -> 1.8 => clamp to 1.0
        val map1 = store.observeWeights().first()
        assertEquals(1.0f, map1["a"]!!, 1e-4f)

        store.bumpNegative(listOf("a"), delta = -1.5f) // 1.0 -1.5 => -0.5 => clamp -1.0
        val map2 = store.observeWeights().first()
        assertEquals(-0.5f, map2["a"]!!, 1e-4f)

        val top = store.getTopCategories(1)
        assertEquals(listOf("a"), top)
    }
}
