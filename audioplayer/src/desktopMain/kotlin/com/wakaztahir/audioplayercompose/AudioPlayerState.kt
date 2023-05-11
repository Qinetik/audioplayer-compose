package com.wakaztahir.audioplayercompose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import java.util.*
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineEvent

actual class AudioPlayerState actual constructor(private val scope: CoroutineScope) {

    actual var state: PlayerPlayState by mutableStateOf(PlayerPlayState.Stopped)
        private set

    actual var audioPath: String = ""
        private set

    actual var progress: Float by mutableStateOf(0f)
        private set

    actual var isReady: Boolean = false
        private set

    private var clip: Clip? = null
    private var updateJob: Job? = null
    private var hasCompleted = false

    private val readyListeners = mutableListOf<() -> Unit>()
    private val completionListeners = mutableListOf<() -> Unit>()
    private fun MutableList<() -> Unit>.invoke() {
        val itr = listIterator()
        while (itr.hasNext()) {
            itr.next().invoke()
            itr.remove()
        }
    }

    private fun getExtension(fileName: String): String? {
        try {
            if (fileName.isEmpty()) return null
            val len: Int = fileName.length
            val ch: Char = fileName[len - 1]
            if (ch == '/' || ch == '\\' || ch == '.') //in the case of . or ..
                return ""
            val dotInd = fileName.lastIndexOf('.')
            val sepInd = fileName.lastIndexOf('/').coerceAtLeast(fileName.lastIndexOf('\\'))
            return if (dotInd <= sepInd) "" else fileName.substring(dotInd + 1).lowercase(Locale.getDefault())
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFromMimeType(type : String?) : String? {
        return when(type){
            "audio/mpeg" -> "mp3"
            "audio/wav" -> "wav"
            "audio/x-aiff" -> "aif"
            "audio/vnd.wav" -> "wav"
            "audio/aac" -> "aac"
            else -> null
        }
    }

    private fun createTemporaryFile(path: String): File {
        val url = URL(path)
        val stream = url.openStream()
        val tempFile = File.createTempFile("temp", getFromMimeType(url.openConnection().contentType) ?: getExtension(path))
        stream.use { inp ->
            tempFile.outputStream().use { out ->
                inp.copyTo(out)
            }
        }
        return tempFile
    }

    actual fun initialize(path: String) {
        this.audioPath = path
        val file =
            if (path.startsWith("http://") || path.startsWith("https://")) createTemporaryFile(path) else File(path)
        val audioInputStream = AudioSystem.getAudioInputStream(file)
        val format = audioInputStream.format
        val info = DataLine.Info(Clip::class.java, format)
        clip = AudioSystem.getLine(info) as? Clip
        clip?.open(audioInputStream)
        clip?.addLineListener {
            if (it.type == LineEvent.Type.STOP) {
                state = PlayerPlayState.Stopped
                hasCompleted = true
                completionListeners.invoke()
            } else if (it.type == LineEvent.Type.OPEN) {
                isReady = true
                readyListeners.invoke()
            }
        }
    }

    actual fun destroyPlayer() {
        stopUpdatingProgress()
        clip?.close()
        clip = null
    }

    actual fun updatePath(path: String) {
        isReady = false
        hasCompleted = false
        initialize(path)
    }

    actual fun startPlaying() {
        if (clip == null) return
        clip?.start()
        startUpdatingProgress()
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

    actual fun seekTo(progress: Float) {
        this.progress = progress
        seekTo((progress * getDuration()).toInt())
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

    actual fun invokeOnReady(block: () -> Unit) {
        if (isReady) {
            block()
        } else {
            readyListeners.add(block)
        }
    }

    actual fun invokeOnCompletion(block: () -> Unit) {
        if (hasCompleted) {
            block()
        } else {
            completionListeners.add(block)
        }
    }
}