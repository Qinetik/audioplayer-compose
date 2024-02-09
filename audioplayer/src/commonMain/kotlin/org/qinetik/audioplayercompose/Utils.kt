package org.qinetik.audioplayercompose

import androidx.compose.runtime.*

@Composable
fun rememberAudioPlayerState(path: String,onAudioNotFound : (Throwable)->Unit = { it.printStackTrace() }): AudioPlayerState {
    val scope = rememberCoroutineScope()
    val state = remember { AudioPlayerState(scope) }
    LaunchedEffect(path) {
        if (state.audioPath != path) {
            state.updatePath(path)
        }
    }
    DisposableEffect(state) {
        try {
            state.initialize(path)
        }catch(e : Exception){
            onAudioNotFound(e)
        }
        onDispose { state.destroyPlayer() }
    }
    return state
}