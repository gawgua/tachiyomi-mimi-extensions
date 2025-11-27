package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.response

import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto.Manga
import kotlinx.serialization.Serializable

@Serializable
data class AllTitleResponse(
    val currentPage: Int,
    val data: List<Manga>,
    val first: Boolean,
    val last: Boolean,
    val pageSize: Int,
    val totalElem: Int,
    val totalPage: Int
)
