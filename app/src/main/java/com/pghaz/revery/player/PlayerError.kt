package com.pghaz.revery.player

abstract class PlayerError(message: String?, throwable: Throwable?) :
    Throwable(message, throwable) {

    /**
     *  General errors
     */
    data class Initialization(val throwable: Throwable?) :
        PlayerError(throwable?.toString(), throwable)

    data class FadeIn(val throwable: Throwable?) : PlayerError(throwable?.toString(), throwable)
    data class FadeOut(val throwable: Throwable?) : PlayerError(throwable?.toString(), throwable)

    /**
     *  Default Player
     */
    data class DefaultPlayerUnknown(val throwable: Throwable?) :
        PlayerError(throwable?.toString(), throwable)

    data class DefaultPlayerIO(val throwable: Throwable?) :
        PlayerError(throwable?.toString(), throwable)

    data class DefaultPlayerMalformed(val throwable: Throwable?) :
        PlayerError(throwable?.toString(), throwable)

    data class DefaultPlayerUnsupported(val throwable: Throwable?) :
        PlayerError(throwable?.toString(), throwable)

    data class DefaultPlayerTimedOut(val throwable: Throwable?) :
        PlayerError(throwable?.toString(), throwable)
}