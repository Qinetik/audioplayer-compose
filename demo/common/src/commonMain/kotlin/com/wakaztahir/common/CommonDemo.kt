package com.wakaztahir.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.wakaztahir.audioplayercompose.rememberAudioPlayerState

@Composable
fun CommonDemo() {
    val audioUrl = "https://samplelib.com/lib/preview/mp3/sample-3s.mp3"
    val audioPlayer = rememberAudioPlayerState(audioUrl)
    LaunchedEffect(null){
        audioPlayer.startPlaying()
    }
}