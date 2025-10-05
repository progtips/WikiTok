package com.example.wikitok.domain.history

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentHistoryStoreTest {
    @Test
    fun same_id_not_more_than_once_in_50() = runTest {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = RecentHistoryStore(ctx, capacity = 50)
        val id = 42L
        store.addShown(id)
        assertTrue(store.wasRecentlyShown(id))
        // add 49 different ids
        for (i in 1..49) store.addShown(id + i)
        // still should be recent
        assertTrue(store.wasRecentlyShown(id))
        // add one more to push out
        store.addShown(9999L)
        assertFalse(store.wasRecentlyShown(id))
    }
}


