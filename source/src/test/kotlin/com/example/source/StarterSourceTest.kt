package com.example.source

import ani.saikou.neo.extension.api.Anime
import ani.saikou.neo.extension.api.AnimeIds
import ani.saikou.neo.extension.api.AnimeTitles
import ani.saikou.neo.extension.api.Edition
import ani.saikou.neo.extension.api.Episode
import ani.saikou.neo.extension.api.Season
import ani.saikou.neo.extension.api.Server
import ani.saikou.neo.extension.api.SourceException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StarterSourceTest {
    // What the app sends for an episode it picked from its catalogue. Use a show your site has.
    private val episode =
        Episode(
            anime =
                Anime(
                    ids = AnimeIds(anilist = 1, mal = 1, anidb = null, kitsu = null),
                    titles = AnimeTitles(romaji = "Sample Show", english = "Sample Show", native = null, synonyms = emptyList()),
                    season = Season.Spring,
                    year = 2020,
                ),
            number = 1,
        )

    private val source = StarterSource()

    @Test
    fun hasNoServersYet() =
        runTest {
            assertEquals(emptyList(), source.servers(episode))
        }

    @Test
    fun hasNoStreamsYet() =
        runTest {
            val server = Server(id = "1", name = "Server 1", edition = Edition.Sub, runtime = null)
            assertFailsWith<SourceException.NotFound> { source.stream(server) }
        }
}
