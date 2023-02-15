package com.rpostman.extension

import androidx.compose.ui.window.DialogWindowScope
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowScope
import java.awt.FileDialog

fun FileDialog(windowScope: WindowScope?, title: String, mode: Int, handler: FileDialog.() -> Unit) {
    when (windowScope) {
        is DialogWindowScope -> {
            FileDialog(windowScope.window, title, mode).apply(handler)
        }

        is FrameWindowScope -> {
            FileDialog(windowScope.window, title, mode).apply(handler)
        }

        else -> error("Unsupported window scope: $windowScope")
    }

}