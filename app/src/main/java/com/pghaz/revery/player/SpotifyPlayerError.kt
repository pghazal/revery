package com.pghaz.revery.player

sealed class SpotifyPlayerError(message: String?, throwable: Throwable?) :
    PlayerError(message, throwable) {

    /**
     *  Spotify Player
     */
    data class SpotifyPlayerUnknown(override val message: String?, val throwable: Throwable?) :
        SpotifyPlayerError(message, throwable)

    data class SpotifyNotLoggedIn(override val message: String?, val throwable: Throwable?) :
        SpotifyPlayerError(message, throwable)

    data class SpotifyPlayerUserNotAuthorized(
        override val message: String?,
        val throwable: Throwable?
    ) : SpotifyPlayerError(message, throwable)

    data class SpotifyPlayerNotInstalled(override val message: String?, val throwable: Throwable?) :
        SpotifyPlayerError(message, throwable)

    data class SpotifyAuthenticationFailed(
        override val message: String?,
        val throwable: Throwable?
    ) : SpotifyPlayerError(message, throwable)

    data class SpotifyUnsupportedFeatureVersion(
        override val message: String?,
        val throwable: Throwable?
    ) : SpotifyPlayerError(message, throwable)

    data class SpotifyOfflineMode(override val message: String?, val throwable: Throwable?) :
        SpotifyPlayerError(message, throwable)

    data class SpotifyRemoteService(override val message: String?, val throwable: Throwable?) :
        SpotifyPlayerError(message, throwable)

    data class SpotifyDisconnected(override val message: String?, val throwable: Throwable?) :
        SpotifyPlayerError(message, throwable)

    data class SpotifyRemoteClient(override val message: String?, val throwable: Throwable?) :
        SpotifyPlayerError(message, throwable)
}