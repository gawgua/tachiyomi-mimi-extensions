package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class Uploader(
    val id: Int,
    val displayName: String,
    val avatarUrl: String
)
