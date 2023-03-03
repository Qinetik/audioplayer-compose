package com.wakaztahir.audioplayercompose

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

    actual var hasPlayed: Boolean = false
        private set

    private var player: MediaPlayer? = null
    private var prepared: Boolean = false
    private var updateJob: Job? = null

    actual fun initialize(path: String) {
        this.audioPath = path
        player = MediaPlayer()
        player!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        player!!.setOnPreparedListener {
            prepared = true
        }
        player!!.setOnCompletionListener {
            state = PlayerPlayState.Stopped
            hasPlayed = true
        }
    }

    actual fun destroyPlayer() {
        stopUpdatingProgress()
        player?.release()
        player = null
    }

    actual fun updatePath(path: String) {
        audioPath = path
    }

    actual fun startPlaying(time: Int) {
        if (player == null) return
        player!!.setDataSource(audioPath)
        player!!.prepare()
        player!!.seekTo(time)
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
        if (player?.isPlaying == false && prepared) {
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
}

@Composable
fun rememberAudioPlayerState(path: String): AudioPlayerState {
    val scope = rememberCoroutineScope()
    val state = remember { AudioPlayerState(scope) }
    LaunchedEffect(path) {
        if (state.audioPath != path) {
            state.updatePath(path)
        }
    }
    DisposableEffect(state) {
        state.initialize(path)
        onDispose { state.destroyPlayer() }
    }
    return state
}