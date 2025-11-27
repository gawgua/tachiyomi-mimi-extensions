package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto

import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.Serializable

@Serializable
data class Manga(
    val id: Int,
    val title: String,
    val differentNames: List<String>,
    val description: String?,
    val coverUrl: String,
    val authors: List<Author>,
    val chapterCount: Int,
    val characters: List<String>,
    val genres: List<Genre>,
    val parody: List<String>,
    val lastUpdated: String,
    val uploader: Uploader
) {
    fun toSManga(): SManga {
        return SManga.create().apply {
            url = "/manga/${this@Manga.id}"
            title = this@Manga.title
            author = if (this@Manga.authors.isNotEmpty()) this@Manga.authors[0].name else null
            description = this@Manga.description
            genre = this@Manga.genres.joinToString(", ") { it.name }
            status = SManga.UNKNOWN
            thumbnail_url = this@Manga.coverUrl
        }
    }
}
