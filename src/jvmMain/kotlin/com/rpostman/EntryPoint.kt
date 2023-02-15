package com.rpostman

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.rpostman.extension.LocalWindowScope

fun main() {
    application {
        Window(
            title = "RPostman",
            onCloseRequest = ::exitApplication
        ) {
            CompositionLocalProvider(
                LocalWindowScope provides this
            ) {
                Application()
            }
        }
    }
}