package com.rpostman

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import com.rpostman.executor.Executor

@Composable
fun Application() {
    MaterialTheme(
        colors = darkColors()
    ) {
        Scaffold {
            Executor()
        }
    }
}