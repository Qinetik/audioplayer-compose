package org.qinetik.audioplayercompose

import kotlinx.coroutines.CoroutineScope

expect class AudioPlayerState(scope: CoroutineScope) {

    var audioPath: String
        private set

    var state: PlayerPlayState
        private set

    var progress: Float
        private set

    var isReady : Boolean
        private set

    fun invokeOnReady(block : ()->Unit)

    fun invokeOnCompletion(block: () -> Unit)

    fun initialize(path: String)

    fun destroyPlayer()

    fun updatePath(path: String)

    fun startPlaying()

    fun startPlaying(time: Int)

    fun pausePlaying()

    fun resumePlaying()

    fun stopPlaying()

    fun seekTo(time: Int)

    fun seekTo(progress : Float)

    fun getDuration(): Int

    fun getCurrentPosition(): Int

    fun startUpdatingProgress()

    fun stopUpdatingProgress()

}