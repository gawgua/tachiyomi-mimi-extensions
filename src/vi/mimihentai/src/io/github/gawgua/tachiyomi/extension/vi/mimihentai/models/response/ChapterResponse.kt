package io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.response

import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto.Chapter
import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto.Image
import kotlinx.serialization.Serializable

@Serializable
data class ChapterResponse(
    val info: Chapter,
    val next: Int?,
    val prev: Int?,
    val pages: List<Image>
)
