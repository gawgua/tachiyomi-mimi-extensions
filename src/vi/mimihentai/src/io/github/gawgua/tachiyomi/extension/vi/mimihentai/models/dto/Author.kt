package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val id: Int,
    val name: String,
    val coverUrl: String,
    val mangaCount: Int
)
