package org.mylibrary.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.singleWindowApplication
import org.qinetik.common.CommonDemo
import org.qinetik.common.rememberColorScheme

fun main() = singleWindowApplication(title = "Markdown Editor") {
    MaterialTheme(rememberColorScheme()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CommonDemo()
        }
    }
}