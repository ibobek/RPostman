package com.rpostman.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.rpostman.extension.FileDialog
import com.rpostman.extension.currentWindowScope

@Composable
fun FilePicker(
    label: String = "",
    placeholder: String = "",
    onChange: (String) -> Unit = {}
) {
    val windowScope = currentWindowScope()
    val path = remember { mutableStateOf("") }

    LaunchedEffect(path.value) {
        onChange(path.value)
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = path.value,
            label = { Text(label) },
            maxLines = 1,
            placeholder = { Text(text = placeholder, fontStyle = FontStyle.Italic) },
            onValueChange = { path.value = it },
            trailingIcon = {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    IconButton(onClick = {
                        FileDialog(windowScope, "Select file", java.awt.FileDialog.LOAD) {
                            isVisible = true
                            if (file != null) {
                                path.value = "${this.directory}${this.file}"
                            } else {
                                path.value = ""
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Browse"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}