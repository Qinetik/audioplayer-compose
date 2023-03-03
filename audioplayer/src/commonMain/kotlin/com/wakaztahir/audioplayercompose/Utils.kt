package com.wakaztahir.audioplayercompose

import androidx.compose.runtime.*


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