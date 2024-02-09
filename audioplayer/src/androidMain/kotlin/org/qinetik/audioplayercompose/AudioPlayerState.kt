package org.qinetik.audioplayercompose

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual class AudioPlayerState actual constructor(private val scope: CoroutineScope) {

    actual var state: PlayerPlayState by mutableStateOf(PlayerPlayState.Stopped)
        private set

    actual var audioPath: String = ""
        private set

    actual var progress: Float by mutableStateOf(0f)
        private set

    actual var isReady: Boolean = false
        private set

    private var hasCompleted: Boolean = false
    private var player: MediaPlayer? = null
    private var updateJob: Job? = null

    private val readyListeners = mutableListOf<() -> Unit>()
    private val completionListeners = mutableListOf<() -> Unit>()
    private fun MutableList<() -> Unit>.invoke() {
        val itr = listIterator()
        while (itr.hasNext()) {
            itr.next().invoke()
            itr.remove()
        }
    }

    actual fun initialize(path: String) {
        this.audioPath = path
        player = MediaPlayer()
        player!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        player!!.setDataSource(audioPath)
        player!!.prepare()
        player!!.setOnPreparedListener {
            isReady = true
            readyListeners.invoke()
        }
        player!!.setOnCompletionListener {
            state = PlayerPlayState.Stopped
            hasCompleted = true
            completionListeners.invoke()
        }
    }

    actual fun destroyPlayer() {
        stopUpdatingProgress()
        player?.release()
        player = null
    }

    actual fun updatePath(path: String) {
        audioPath = path
        isReady = false
        hasCompleted = false
        player!!.setDataSource(audioPath)
        player!!.prepare()
    }

    actual fun startPlaying(time: Int) {
        if (player == null) return
        player!!.seekTo(time)
        startPlaying()
    }

    actual fun startPlaying() {
        if (player == null) return
        hasCompleted = false
        player!!.start()
        startUpdatingProgress()
    }

    actual fun pausePlaying() {
        if (player?.isPlaying == true) {
            player!!.pause()
            stopUpdatingProgress()
        }
    }

    actual fun resumePlaying() {
        if (player?.isPlaying == false && isReady) {
            player!!.start()
            startUpdatingProgress()
        }
    }

    actual fun stopPlaying() {
        if (player?.isPlaying == true) {
            player?.stop()
        }
    }


    actual fun seekTo(time: Int) {
        player?.seekTo(time)
    }

    actual fun seekTo(progress: Float) {
        this.progress = progress
        seekTo((getDuration() * progress).toInt())
    }

    actual fun getDuration(): Int {
        return player?.duration ?: 0
    }

    actual fun getCurrentPosition(): Int {
        return player?.currentPosition ?: 0
    }

    actual fun startUpdatingProgress() {
        updateJob = scope.launch { updateProgressLoop(300) }
    }

    actual fun stopUpdatingProgress() {
        updateJob?.cancel()
        updateJob = null
    }

    private suspend fun updateProgressLoop(delayTime: Long) {
        updateProgress()
        delay(delayTime)
        updateProgressLoop(delayTime)
    }

    private fun updateProgress() {
        val currentPosition = getCurrentPosition()
        val duration = getDuration()
        progress = try {
            if (currentPosition != 0 && duration != 0) {
                (currentPosition.toFloat() / duration)
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
        if (currentPosition == duration) {
            state = PlayerPlayState.Stopped
        }
    }

    actual fun invokeOnReady(block: () -> Unit) {
        if (isReady) {
            block()
        } else {
            readyListeners.add(block)
        }
    }

    actual fun invokeOnCompletion(block: () -> Unit) {
        completionListeners.add(block)
    }

}