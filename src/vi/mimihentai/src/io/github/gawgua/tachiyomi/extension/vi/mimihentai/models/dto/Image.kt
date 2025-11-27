package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val imageUrl: String,
    val drm: String
)
