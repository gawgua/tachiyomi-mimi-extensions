package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    val id: Int,
    val name: String,
    val description: String?,
    val mangaCount: Int
)
