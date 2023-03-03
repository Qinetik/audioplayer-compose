package com.wakaztahir.audioplayercompose

import kotlinx.coroutines.CoroutineScope

expect class AudioPlayerState(scope: CoroutineScope) {

    var audioPath: String
        private set

    var state: PlayerPlayState
        private set

    var progress: Float
        private set

    var hasPlayed: Boolean
        private set

    fun initialize(path: String)

    fun destroyPlayer()

    fun updatePath(path: String)

    fun startPlaying(time: Int = 0)

    fun pausePlaying()

    fun resumePlaying()

    fun stopPlaying()

    fun seekTo(time: Int)

    fun getDuration(): Int

    fun getCurrentPosition(): Int

    fun startUpdatingProgress()

    fun stopUpdatingProgress()

}