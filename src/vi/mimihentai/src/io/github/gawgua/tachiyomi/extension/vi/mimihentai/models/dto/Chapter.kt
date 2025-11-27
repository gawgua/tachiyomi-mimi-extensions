package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto

import eu.kanade.tachiyomi.source.model.SChapter
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter

@Serializable
data class Chapter(
    val id: Int,
    val title: String,
    val manga: Manga,
    val order: Int,
    val likes: Int,
    val createdAt: String
) {
    fun toSChapter(): SChapter {
        return SChapter.create().apply {
            url = "/chapter/${this@Chapter.id}"
            name = this@Chapter.title
            chapter_number = this@Chapter.order.toFloat()
        }
    }
}
