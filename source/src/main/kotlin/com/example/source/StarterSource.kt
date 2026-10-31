package com.example.source

import ani.saikou.neo.extension.api.Episode
import ani.saikou.neo.extension.api.Server
import ani.saikou.neo.extension.api.SourceException
import ani.saikou.neo.extension.api.SourceExtension
import ani.saikou.neo.extension.api.Stream

/**
 * Your source's code. Saikou Neo picks the anime and episode from its own
 * catalogue, then calls [servers] to ask where it can be played, and [stream]
 * once the viewer picks a server.
 */
class StarterSource : SourceExtension {
    /**
     * Find [episode] on your site and list the servers that have it.
     *
     * The episode has the anime's titles (`episode.anime.titles.romaji`,
     * `.english`), its AniList and MyAnimeList ids, and the episode number
     * (`episode.number`). Return one server for each place it plays from:
     *
     * ```
     * Server(id = "<what stream() needs, such as a link>", name = "Server 1", edition = Edition.Sub, runtime = null)
     * ```
     *
     * An empty list tells the app this source doesn't have the episode.
     */
    override suspend fun servers(episode: Episode): List<Server> = emptyList()

    /**
     * Get the video for a server from [servers]. `server.id` is whatever you put
     * in it there. Return each quality with its link:
     *
     * ```
     * Stream(qualities = listOf(Quality(height = 1080, url = "https://…/1080.m3u8", format = StreamFormat.Hls)))
     * ```
     *
     * Add `headers` if the site needs them, and `subtitles` if it serves them
     * apart from the video. When something goes wrong, throw a
     * [SourceException], such as [SourceException.NotFound], so the app can say why.
     */
    override suspend fun stream(server: Server): Stream = throw SourceException.NotFound()
}
