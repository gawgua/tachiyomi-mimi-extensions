package io.github.gawgua.tachiyomi.extension.vi.mimihentai

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto.Chapter
import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto.Genre
import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto.Manga
import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.dto.GENRES
import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.response.AllTitleResponse
import io.github.gawgua.tachiyomi.extension.vi.mimihentai.models.response.ChapterResponse
import kotlinx.serialization.decodeFromString
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import uy.kohesive.injekt.injectLazy
import kotlinx.serialization.json.Json
import rx.Observable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MimiHentai : HttpSource() {
    override val name = "MimiHentai"
    override val baseUrl = "https://mimihentai.com"
    override val lang = "vi"
    override val supportsLatest = true

    private val json: Json by injectLazy()

    companion object {
        private val API_URL = "https://api.mimihentai.com/api/v2/manga/".toHttpUrl()

        private const val POPULAR_MANGA_LIMIT = 24

        private enum class TimeType(val value: Int) {
            DAY(0),
            WEEK(1),
            MONTH(2),
        }
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val data = response.parseAs<List<Chapter>>()
        return data.map {
            it.toSChapter().apply {
                date_upload = dateFormatter.parse(
                    it.createdAt.replace("T", " ").dropLast(2)
                )?.time ?: 0L
            }
        }
    }

    override fun chapterListRequest(manga: SManga): Request {
        // https://api.mimihentai.com/api/v2/manga/gallery/{id}
        val id = manga.url.split("/").last()
        val url = API_URL.newBuilder().apply {
            addPathSegments("gallery")
            addPathSegment(id)
        }.build().toString()

        return GET(url)
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("")
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        val manga = response.parseAs<AllTitleResponse>().data
        val mangas = manga.map { it.toSManga() }

        return MangasPage(mangas, true)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        // https://api.mimihentai.com/api/v2/manga/tatcatruyen?page=0&sort=updated_at&ex=196&reup=false
        val url = API_URL.newBuilder().apply {
            addPathSegments("tatcatruyen")
            addQueryParameter("page", (page - 1).toString()) // Tachiyomi starts from 1
            addQueryParameter("sort", "updated_at")
            addQueryParameter("ex", "196")
            addQueryParameter("reup", "false")
        }.build().toString()

        return GET(url)
    }


    // All metadata is gotten through search, popular,...
    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        return Observable.just(manga)
    }

    override fun mangaDetailsParse(response: Response): SManga {
        throw UnsupportedOperationException("")
    }

    override fun pageListParse(response: Response): List<Page> {
        val data = response.parseAs<ChapterResponse>().pages
        return data.mapIndexed { index, image ->
            Page(index, imageUrl = image.imageUrl)
        }
    }

    override fun pageListRequest(chapter: SChapter): Request {
        // https://api.mimihentai.com/api/v2/manga/chapter?id={chapter_id}
        val url = API_URL.newBuilder().apply {
            addPathSegments("chapter")
            addQueryParameter("id", chapter.url.split("/").last())
        }.build().toString()

        return GET(url)
    }

    override fun popularMangaRequest(page: Int): Request {
        // https://api.mimihentai.com/api/v2/manga/top-manga?timeType=0&limit=24
        val url = API_URL.newBuilder().apply {
            addPathSegments("top-manga")
            addQueryParameter("timeType", TimeType.DAY.value.toString())
            addQueryParameter("limit", POPULAR_MANGA_LIMIT.toString())
        }.build().toString()
        return GET(url)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val data = response.parseAs<List<Manga>>()
        val mangas = data.map { it.toSManga() }

        // this api dont support pagination, so no next page
        return MangasPage(mangas, false)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val data = response.parseAs<AllTitleResponse>().data
        val mangas = data.map { it.toSManga() }

        return MangasPage(mangas, true)
    }

    override fun searchMangaRequest(
        page: Int,
        query: String,
        filters: FilterList,
    ): Request {
        // https://api.mimihentai.com/api/v2/manga/advance-search?sort=updated_at&author&parody&character&max=18&genre=416,417&ex=196&page=0&name
        var genreStr = ""
        if (filters.isNotEmpty()) {
            val group = filters[0];
            if (group is GenreGroup) {
                val genres = group.state.mapNotNull { if (it.state == Filter.TriState.STATE_INCLUDE) it.name else null }
                genreStr = genres.joinToString(",") { e ->
                    GENRES.find { it.name == e }?.id.toString()
                }
            }
        }

        val url = API_URL.newBuilder().apply {
            addPathSegments("advance-search")
            addQueryParameter("sort", "updated_at")
            addQueryParameter("page", (page - 1).toString())
            addQueryParameter("genre", genreStr)
            addQueryParameter("ex", "196")
            addQueryParameter("max", POPULAR_MANGA_LIMIT.toString())
            addQueryParameter("name", query)
        }.build().toString()

        return GET(url)
    }

    override fun getFilterList(): FilterList {
        return FilterList(GenreGroup(GENRES))
    }

    private inline fun <reified T> Response.parseAs(): T {
        return json.decodeFromString(this.body.string())
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
    }
}

private class GenreState(genre: Genre) : Filter.TriState (
    name = genre.name,
)

private class GenreGroup(list: List<Genre>) : Filter.Group<Filter.TriState> (
    "Thể loại",
    list.map { GenreState(it) }
)
