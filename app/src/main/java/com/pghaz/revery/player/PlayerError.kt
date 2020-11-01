package com.pghaz.revery.player

sealed class PlayerError(throwable: Throwable?) : Throwable(throwable) {

    // General errors
    data class Initialization(val throwable: Throwable?) : PlayerError(throwable)
    data class FadeIn(val throwable: Throwable?) : PlayerError(throwable)

    // Default Player
    data class DefaultPlayerUnknown(val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerIO(val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerMalformed(val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerUnsupported(val throwable: Throwable?) : PlayerError(throwable)
    data class DefaultPlayerTimedOut(val throwable: Throwable?) : PlayerError(throwable)

    // Spotify
    data class SpotifyPlayerUnknown(val throwable: Throwable?) : PlayerError(throwable)
    data class SpotifyPlayerUserNotAuthorized(val throwable: Throwable?) :
        PlayerError(throwable)

    data class SpotifyPlayerNotInstalled(val throwable: Throwable?) :
        PlayerError(throwable)
}