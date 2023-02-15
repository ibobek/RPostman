package com.rpostman.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.WindowScope

val LocalWindowScope = staticCompositionLocalOf<WindowScope> {
    error("LocalWindowScope not provided")
}

@Composable
fun currentWindowScope(): WindowScope = LocalWindowScope.current