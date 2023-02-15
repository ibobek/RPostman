package com.rpostman.executor

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lordcodes.turtle.ProcessCallbacks
import com.lordcodes.turtle.shellRun
import com.rpostman.executor.settings.Settings
import com.rpostman.executor.settings.SettingsState
import com.rpostman.executor.settings.rememberSettings
import com.rpostman.widget.Logs
import com.rpostman.widget.rememberLogsController
import kotlinx.coroutines.*
import java.util.concurrent.Executors

val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    println(throwable)
}

@Composable
fun Executor() {
    val logsController = rememberLogsController()
    val processed = remember { mutableStateOf(0) }
    val working = remember { mutableStateOf(false) }
    val settings = rememberSettings()
    val executionScope = rememberCoroutineScope { Executors.newCachedThreadPool().asCoroutineDispatcher() + exceptionHandler }

    LaunchedEffect(settings.value) {
        println(settings.value)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Settings(state = settings)
        Row(
            modifier = Modifier.align(Alignment.Start)
        ) {
            Button(
                onClick = {
                    if (working.value) {
                        executionScope.coroutineContext.cancelChildren()
                    } else {
                        execute(working, processed, settings.value, executionScope)
                    }
                },
                modifier = Modifier.padding(0.dp)
            ) {
                Text(text = if (working.value) "Stop ${processed.value} / ${settings.value.concurrency}" else "Start")
            }
        }
        Card {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                ) {
                    Text(text = "Logs", style = MaterialTheme.typography.h6)
                    IconButton(onClick = { logsController.value.clear() }) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Clear Logs",
                        )
                    }
                }
                Divider()
                Logs(controller = logsController)
            }
        }
    }
}

fun execute(
    working: MutableState<Boolean>,
    processed: MutableState<Int>,
    settings: SettingsState,
    scope: CoroutineScope
) {
    val synchronizedScope = scope + Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    val synchronizedRun = { block: suspend () -> Unit -> synchronizedScope.launch { block() } }
    val independentScope = scope + SupervisorJob()

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        System.err.println(throwable.message)
    }

    independentScope.launch(exceptionHandler) {
        println("Starting ${settings.concurrency} jobs")
        synchronizedRun { processed.value = 0 }
        working.value = true

        val jobs = mutableListOf<Deferred<String>>()
        repeat(settings.concurrency) {
            println("Starting job $it")
            val job = scope.async(exceptionHandler) {
                shellRun {
                    println("Execute newman")
                    commandStreaming(
                        command = "newman",
                        arguments = listOf(
                            "run", settings.path,
                            "-n", settings.iterations.toString(),
                            "--color", "off",
                            "--bail",
                            "--disable-unicode",
                            "--reporters", "cli",
                        ),
                        callbacks = object : ProcessCallbacks {
                            override fun onProcessStart(process: Process) {
                                println("Newman process started")
                                val ignoreException = CoroutineExceptionHandler { _, _ -> }

                                scope.launch {
                                    suspendCancellableCoroutine<Unit> {
                                        it.invokeOnCancellation {
                                            process.destroyForcibly()
                                        }
                                    }
                                }
                                scope.launch {
                                    try {
                                        synchronizedRun { processed.value++ }
                                        process.onExit().join()
                                    } finally {
                                        synchronizedRun { processed.value-- }
                                    }
                                }
                                scope.launch(SupervisorJob() + ignoreException) {
                                    process.inputStream.transferTo(System.out)
                                }
                                scope.launch(SupervisorJob() + ignoreException) {
                                    process.errorStream.transferTo(System.err)
                                }
                            }
                        }
                    )
                    println("Newman process finished")
                    "Finished"
                }
            }
            jobs.add(job)
        }

        println("Waiting for jobs to finish")
        jobs.joinAll()
        working.value = false
        println("All jobs finished")
    }
}