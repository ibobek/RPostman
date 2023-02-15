package com.rpostman.widget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

@Composable
fun Scrollable(
    onIncreaseContent: (scroll: ScrollState) -> Unit = {},
    content: @Composable RowScope.(modifier: Modifier) -> Unit
) {
    val showScrollbar = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    DisposableEffect(scrollState.maxValue) {
        val previousMaxValue = scrollState.maxValue
        onDispose {
            if (previousMaxValue < scrollState.maxValue) {
                onIncreaseContent(scrollState)
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        content(Modifier.verticalScroll(scrollState)
            .onGloballyPositioned {
                showScrollbar.value = scrollState.maxValue != 0
            }
        )

        if (showScrollbar.value) {
            Spacer(modifier = Modifier.width(8.dp))
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}