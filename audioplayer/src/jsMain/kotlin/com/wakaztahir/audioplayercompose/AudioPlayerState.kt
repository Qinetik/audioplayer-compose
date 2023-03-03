package com.wakaztahir.audioplayercompose

import kotlinx.coroutines.CoroutineScope
import org.w3c.dom.Audio
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

actual class AudioPlayerState actual constructor(scope: CoroutineScope) {

    actual var audioPath: String = ""
        private set

    actual var state: PlayerPlayState = PlayerPlayState.Stopped
        private set

    actual var progress: Float = 0f
        private set

    actual var hasPlayed: Boolean = false
        private set

    private var elem: Audio? = null

    actual fun initialize(path: String) {
        this.audioPath = path
        this.elem = Audio(path)
    }

    actual fun destroyPlayer() {
        elem = null
    }

    actual fun updatePath(path: String) {
        audioPath = path
    }

    actual fun startPlaying(time: Int) = seekAndAfter(time) {
        startUpdatingProgress()
        elem?.play()
    }

    actual fun pausePlaying() {
        elem?.pause()
    }

    actual fun resumePlaying() {
        elem?.play()
    }

    actual fun stopPlaying() {
        elem?.pause()
    }

    private fun seekAndAfter(time: Int, after: () -> Unit) {
        if (elem?.readyState?.toInt() == 4) {
            // The audio is ready to play
            elem?.currentTime = time.toDouble()
            after()
        } else {
            elem?.addEventListener("canplaythrough", {
                elem?.currentTime = time.toDouble()
                after()
            })
        }
    }

    actual fun seekTo(time: Int) = seekAndAfter(time) {
        // do nothing
    }

    actual fun getDuration(): Int {
        return 0
    }

    actual fun getCurrentPosition(): Int {
        return 0
    }

    private val ProgressUpdater = object : EventListener {
        override fun handleEvent(event: Event) {
            val currentTime = elem?.currentTime ?: 1.0
            val duration = elem?.duration ?: 1.0
            this@AudioPlayerState.progress = (currentTime / duration).toFloat()
        }
    }

    actual fun startUpdatingProgress() {
        elem?.addEventListener("timeupdate", ProgressUpdater)
    }

    actual fun stopUpdatingProgress() {
        elem?.removeEventListener("timeupdate", ProgressUpdater)
    }

}