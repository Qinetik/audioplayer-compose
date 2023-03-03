package com.wakaztahir.audioplayercompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.*
import java.io.File
import javax.sound.sampled.*

actual class AudioPlayerState actual constructor(private val scope: CoroutineScope) {

    actual var state: PlayerPlayState by mutableStateOf(PlayerPlayState.Stopped)
        private set

    actual var audioPath: String = ""
        private set

    actual var progress: Float by mutableStateOf(0f)
        private set

    actual var hasPlayed: Boolean = false
        private set

    private var clip: Clip? = null
    private var updateJob: Job? = null

    actual fun initialize(path: String) {
        this.audioPath = path
        val file = File(path)
        val audioInputStream = AudioSystem.getAudioInputStream(file)
        val format = audioInputStream.format
        val info = DataLine.Info(Clip::class.java, format)
        clip = AudioSystem.getLine(info) as? Clip
        clip?.open(audioInputStream)
        clip?.addLineListener {
            if (it.type == LineEvent.Type.STOP) {
                state = PlayerPlayState.Stopped
                hasPlayed = true
            }
        }
    }

    actual fun destroyPlayer() {
        stopUpdatingProgress()
        clip?.close()
        clip = null
    }

    actual fun updatePath(path: String) {
        audioPath = path
    }

    actual fun startPlaying(time: Int) {
        if (clip == null) return
        clip?.microsecondPosition = time.toLong() * 1000
        clip?.start()
        startUpdatingProgress()
    }

    actual fun pausePlaying() {
        if (clip?.isRunning == true) {
            clip?.stop()
            stopUpdatingProgress()
        }
    }

    actual fun resumePlaying() {
        if (clip?.isRunning == false) {
            clip?.start()
            startUpdatingProgress()
        }
    }

    actual fun stopPlaying() {
        if (clip?.isRunning == true) {
            clip?.stop()
            clip?.microsecondPosition = 0
        }
    }

    actual fun seekTo(time: Int) {
        clip?.microsecondPosition = time.toLong() * 1000
    }

    actual fun getDuration(): Int {
        return clip?.microsecondLength?.toInt()?.div(1000) ?: 0
    }

    actual fun getCurrentPosition(): Int {
        return clip?.microsecondPosition?.toInt()?.div(1000) ?: 0
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
}