package com.example.wikitok.data.wiki

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WikiDtosMapperTest {

    @Test
    fun `toDomain maps fields with pageid and extract`() {
        val dto = WikiSummaryDto(
            title = "Test Title",
            extract = "Summary",
            description = null,
            thumbnail = WikiSummaryDto.Thumbnail(source = "https://img"),
            pageid = 1234L
        )

        val domain = dto.toDomain(categories = listOf("Cat1", "Cat2"))

        assertThat(domain.pageId).isEqualTo(1234L)
        assertThat(domain.title).isEqualTo("Test Title")
        assertThat(domain.summary).isEqualTo("Summary")
        assertThat(domain.imageUrl).isEqualTo("https://img")
        assertThat(domain.categories).containsExactly("Cat1", "Cat2").inOrder()
    }

    @Test
    fun `toDomain falls back to description when extract is null`() {
        val dto = WikiSummaryDto(
            title = "T",
            extract = null,
            description = "Desc",
            thumbnail = null,
            pageid = null
        )

        val domain = dto.toDomain()

        assertThat(domain.pageId).isEqualTo(0L)
        assertThat(domain.summary).isEqualTo("Desc")
        assertThat(domain.imageUrl).isNull()
        assertThat(domain.categories).isEmpty()
    }
}


