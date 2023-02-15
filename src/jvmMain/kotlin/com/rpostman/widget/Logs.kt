package com.rpostman.widget

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.ConcurrentLinkedDeque

private inline fun <reified T> ConcurrentLinkedDeque<T>.last(count: Int): Array<T> {
    return descendingIterator().asSequence().take(count).toList().toTypedArray()
}

data class LogsController(
    val clear: () -> Unit = {}
)

@Composable
fun rememberLogsController(): MutableState<LogsController> {
    return remember { mutableStateOf(LogsController()) }
}

@Composable
fun Logs(
    controller: MutableState<LogsController> = rememberLogsController()
) {
    val listState = rememberLazyListState()
    val data = remember { mutableStateListOf<String>() }
    val dataSynchronizedScope = rememberCoroutineScope()

    fun createOutputStream(share: PrintStream) = object : OutputStream() {
        private val lineSeparator = System.lineSeparator().toByteArray(Charsets.UTF_8)
        private val buffer = ConcurrentLinkedDeque<Byte>()

        override fun write(value: Int) {
            share.write(value)
            buffer.addLast(value.toByte())
            if (buffer.last(lineSeparator.size).toByteArray().contentEquals(lineSeparator)) {
                buffer.removeLast()
                val line = String(buffer.toByteArray(), Charsets.UTF_8)
                buffer.clear()
                dataSynchronizedScope.launch {
                    data.add(line)
                    if (data.size > 10000) {
                        data.removeFirst()
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        controller.value = controller.value.copy(clear = {
            dataSynchronizedScope.launch { data.clear() }
        })
        onDispose { }
    }

    DisposableEffect(Unit) {
        val originalOut = System.out
        val originalError = System.err

        val currentOut = PrintStream(createOutputStream(originalOut))
        val currentError = PrintStream(createOutputStream(originalError))

        System.setOut(currentOut)
        System.setErr(currentError)

        onDispose {
            System.setOut(originalOut)
            System.setOut(originalError)
        }
    }
//
//    LaunchedEffect(drawControls) {
//        drawControls {
//            IconButton(onClick = { data.clear() }) {
//                Icon(
//                    imageVector = Icons.Default.CleaningServices,
//                    contentDescription = "Clear Logs",
//                )
//            }
//        }
//    }

    SelectionContainer(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f, false),
            ) {
                items(data.toList()) {
                    Text(
                        text = it,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle.Default,
                    )
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.fillMaxHeight().width(16.dp),
            )
        }
    }
}
