package com.wakaztahir.audioplayercompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import org.w3c.dom.Audio
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

actual class AudioPlayerState actual constructor(private val scope: CoroutineScope) {

    actual var audioPath: String = ""
        private set

    actual var state: PlayerPlayState by mutableStateOf(PlayerPlayState.Stopped)
        private set

    actual var progress: Float by mutableStateOf(0f)

    actual var isReady: Boolean
        get() = elem?.readyState?.toInt() == 4
        set(_) {
            // do nothing
        }

    private val readyListeners = mutableListOf<() -> Unit>()
    private val completionListeners = mutableListOf<() -> Unit>()
    private fun MutableList<() -> Unit>.invoke() {
        val itr = listIterator()
        while (itr.hasNext()) {
            itr.next().invoke()
            itr.remove()
        }
    }

    private var elem: Audio? = null

    actual fun initialize(path: String) {
        this.audioPath = path
        this.elem = Audio(path).apply {
            addEventListener("ended", {
                completionListeners.invoke()
            })
        }
    }

    actual fun destroyPlayer() {
        elem = null
    }

    actual fun updatePath(path: String) {
        audioPath = path
    }

    actual fun startPlaying(time: Int) {
        elem?.currentTime = time.toDouble()
        startUpdatingProgress()
        elem?.play()
    }

    actual fun startPlaying() {
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


    actual fun seekTo(time: Int) {
        elem?.currentTime = time.toDouble()
    }

    actual fun seekTo(progress: Float) {
        seekTo((progress * getDuration()).toInt())
    }

    actual fun getDuration(): Int {
        return elem?.duration?.toInt() ?: 0
    }

    actual fun getCurrentPosition(): Int {
        return elem?.currentTime?.toInt() ?: 0
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

    actual fun invokeOnReady(block: () -> Unit) {
        if (elem?.readyState?.toInt() == 4) {
            // The audio is ready to play
            block()
        } else {
            elem?.addEventListener("canplaythrough", {
                block()
            })
        }
    }

    actual fun invokeOnCompletion(block: () -> Unit) {
        completionListeners.add(block)
    }

}