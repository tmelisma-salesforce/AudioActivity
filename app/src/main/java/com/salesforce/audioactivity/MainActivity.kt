package com.salesforce.audioactivity

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salesforce.audioactivity.ui.theme.AudioActivityTheme
import kotlinx.coroutines.flow.filter

class MainActivity : ComponentActivity() {

    private val viewModel: AudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display

        setContent {
            AudioActivityTheme(darkTheme = true) {
                val activityLevel by viewModel.activityLevel.collectAsStateWithLifecycle()
                val hasPermission by viewModel.permissionGranted.collectAsStateWithLifecycle()
                val context = LocalContext.current
                // Use the non-deprecated LocalLifecycleOwner
                val lifecycleOwner = LocalLifecycleOwner.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    viewModel.updatePermissionStatus(context)
                }

                // Effect to manage audio processing based on lifecycle events
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.updatePermissionStatus(context)
                        } else if (event == Lifecycle.Event.ON_PAUSE) {
                            viewModel.stopAudioProcessing()
                        }
                    }
                    val lifecycle = lifecycleOwner.lifecycle
                    lifecycle.addObserver(observer)
                    onDispose {
                        lifecycle.removeObserver(observer)
                        viewModel.stopAudioProcessing()
                    }
                }

                // Effect to handle initial permission check/request and auto-start audio
                LaunchedEffect(Unit) {
                    viewModel.updatePermissionStatus(context)
                    if (!viewModel.permissionGranted.value) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }

                    // Observe permission changes to auto-start AFTER granted
                    snapshotFlow { viewModel.permissionGranted.value }
                        .filter { it } // Only proceed when permission is true
                        .collect { isGranted ->
                            if (isGranted) {
                                viewModel.startAudioProcessing()
                            }
                        }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.systemBars) // Respect system bars for edge-to-edge
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Show a message if permission is not yet granted
                        if (!hasPermission) {
                            Text(
                                "Microphone permission needed for audio visualization.",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Display the animated audio circle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            AudioCircle(
                                activityLevel = activityLevel,
                                // modifier = Modifier, // Modifier can be passed if needed
                                baseSize = 150.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Preview Function ---
@Preview(showBackground = true, backgroundColor = 0xFF023248)
@Composable
fun CirclePreview() {
    AudioActivityTheme(darkTheme = true) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f)
            ) {
                AudioCircle(activityLevel = 0.5f, baseSize = 150.dp)
            }
        }
    }
}
