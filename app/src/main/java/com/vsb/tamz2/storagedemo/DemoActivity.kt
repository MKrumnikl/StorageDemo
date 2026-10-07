package com.vsb.tamz2.storagedemo

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.vsb.tamz2.storagedemo.ui.theme.StorageDemoTheme
import kotlinx.coroutines.*

// Shared UI and background execution; storage operations stay in each Activity.
abstract class DemoActivity : ComponentActivity() {
    private var busy by mutableStateOf(false)
    protected var input by mutableStateOf("Hello storage!")
    // Compose observes output; assigning a new value refreshes Text(output).
    protected var output by mutableStateOf("Use the buttons to inspect stored data.")

    protected fun screen(
        title: String,
        explanation: String,
        actions: List<Pair<String, () -> Unit>>,
        extra: @Composable () -> Unit = {}
    ) {
        setContent {
            StorageDemoTheme {
                Scaffold { padding ->
                    Column(
                        Modifier.padding(padding).padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                        Text(explanation)
                        var text by rememberSaveable {
                            mutableStateOf(input)
                        }
                        SideEffect {
                            input = text
                        }
                        OutlinedTextField(
                            text,
                            {
                                text = it
                                input = it
                            },
                            label = {
                                Text("Demo value")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        actions.forEach { (label, action) ->
                            Button(
                                onClick = action,
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label)
                            }
                        }
                        extra()
                        Text(output, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    // File/database work runs off the main thread; lifecycleScope cancels on destruction.
    protected fun work(codeBlock: suspend () -> String) {
        if (busy) return
        busy = true
        lifecycleScope.launch {
            try {
                // Run the supplied codeBlock on IO, then display its returned String on the main thread.
                output = withContext(Dispatchers.IO) {
                    codeBlock()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                output = "${e.javaClass.simpleName}: ${e.message}"
            } finally {
                busy = false
            }
        }
    }
}
