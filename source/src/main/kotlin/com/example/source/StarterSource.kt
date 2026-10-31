package com.example.source

import ani.saikou.neo.extension.api.Episode
import ani.saikou.neo.extension.api.Server
import ani.saikou.neo.extension.api.SourceException
import ani.saikou.neo.extension.api.SourceExtension
import ani.saikou.neo.extension.api.Stream

/**
 * Where your source starts. Saikou Neo picks the anime and episode from its own
 * catalogue and asks the source where to play it. Rename this class and set
 * source.class in gradle.properties to match.
 */
class StarterSource : SourceExtension {
    /**
     * Find [episode] on your site and list the servers that have it. An empty
     * list tells the app this source doesn't have the episode.
     */
    override suspend fun servers(episode: Episode): List<Server> = emptyList()

    /** The qualities, tracks and headers the player needs for one of your servers. */
    override suspend fun stream(server: Server): Stream = throw SourceException.NotFound()
}
