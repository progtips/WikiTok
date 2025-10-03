package com.example.wikitok.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LikedArticleDaoTest {
    private lateinit var db: WikiTokDb
    private lateinit var dao: LikedArticleDao

    @Before
    fun setUp() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(ctx, WikiTokDb::class.java).build()
        dao = db.likedArticleDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_read() = runBlocking {
        val e = LikedArticleEntity(
            pageId = 42L,
            title = "Title",
            thumbnailUrl = null,
            categoriesJson = "[\"A\",\"B\"]",
            likedAt = 1L
        )

        dao.insertOrIgnore(e)
        assertTrue(dao.isLiked(42L).first())

        val all = dao.getAllLiked().first()
        assertEquals(1, all.size)
        assertEquals("Title", all[0].title)
    }
}


