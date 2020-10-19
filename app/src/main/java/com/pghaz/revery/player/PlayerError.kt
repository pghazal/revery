package com.pghaz.revery.player

sealed class PlayerError(open val throwable: Throwable?) : Throwable(throwable) {

    // General errors
    data class Initialization(override val throwable: Throwable?) : PlayerError(throwable)
    data class FadeIn(override val throwable: Throwable?) : PlayerError(throwable)

    // Default Player
    data class DefaultPlayerUnknown(override val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerIO(override val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerMalformed(override val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerUnsupported(override val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerTimedOut(override val throwable: Throwable?) : PlayerError(throwable)

    // Spotify
    data class SpotifyPlayerUnknown(override val throwable: Throwable?) : PlayerError(throwable)
    data class SpotifyPlayerUserNotAuthorized(override val throwable: Throwable?) :
        PlayerError(throwable)

    data class SpotifyPlayerNotInstalled(override val throwable: Throwable?) :
        PlayerError(throwable)
}