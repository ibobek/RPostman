package com.rpostman.executor.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.rpostman.widget.FilePicker

data class SettingsState(
    val path: String = "",
    val concurrency: Int = 1,
    val iterations: Int = 1,
)

@Composable
fun rememberSettings() = remember { mutableStateOf(SettingsState()) }

@Composable
fun Settings(state: MutableState<SettingsState> = rememberSettings()) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        FilePicker(
            label = "Collection",
            placeholder = "Path to collection",
            onChange = { state.value = state.value.copy(path = it) }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Input(
            value = state.value.concurrency.toString(),
            onChange = { state.value = state.value.copy(concurrency = it.toIntOrNull() ?: 1) },
            label = "Concurrency",
            placeholder = "Number of concurrent workers"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Input(
            value = state.value.iterations.toString(),
            onChange = { state.value = state.value.copy(iterations = it.toIntOrNull() ?: 1) },
            label = "Iterations",
            placeholder = "Number of iterations"
        )
    }
}

@Composable
private fun Input(
    value: String,
    onChange: (String) -> Unit = {},
    label: String = "",
    placeholder: String = ""
) {
    val stateValue = remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        stateValue.value = value
    }

    TextField(
        value = stateValue.value,
        onValueChange = { stateValue.value = it },
        label = { Text(label) },
        placeholder = { Text(placeholder, fontStyle = FontStyle.Italic) },
        maxLines = 1,
        modifier = Modifier.fillMaxWidth()
            .onFocusChanged {
                if (!it.isFocused) {
                    onChange(stateValue.value)
                }
            }
    )
}