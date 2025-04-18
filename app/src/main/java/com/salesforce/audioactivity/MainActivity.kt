package com.salesforce.audioactivity // Your package name

// --- Android & Activity Imports ---
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

// --- Compose UI Imports ---
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.Row // No longer needed
import androidx.compose.foundation.layout.Spacer // Keep if needed for layout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height // Keep if needed for layout
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.width // No longer needed
// import androidx.compose.material3.Button // No longer needed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text // Keep for potential messages
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow // Import snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color // For text color

// --- Lifecycle Imports ---
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// --- Coroutine Imports ---
import kotlinx.coroutines.flow.filter // Import filter

// --- Project-Specific Imports ---
import com.salesforce.audioactivity.ui.theme.AudioActivityTheme
import com.salesforce.audioactivity.AudioCircle
import com.salesforce.audioactivity.AudioViewModel

// --- MainActivity Class ---
class MainActivity : ComponentActivity() {

    private val viewModel: AudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()

        setContent {
            AudioActivityTheme(darkTheme = true) {
                val activityLevel by viewModel.activityLevel.collectAsStateWithLifecycle()
                val hasPermission by viewModel.permissionGranted.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                // Permission Request Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    viewModel.updatePermissionStatus(context) // Update status based on result
                    // No automatic start here, will be handled by LaunchedEffect below
                }

                // Effect to manage lifecycle events (checking permission, stopping audio)
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

                // Effect to handle initial permission request and auto-start audio
                LaunchedEffect(Unit) { // Run once when the activity launches
                    viewModel.updatePermissionStatus(context) // Initial check
                    if (!viewModel.permissionGranted.value) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }

                    // Observe permission changes to auto-start AFTER granted
                    // Use snapshotFlow to react to Compose state changes within LaunchedEffect
                    snapshotFlow { viewModel.permissionGranted.value }
                        .filter { it } // Only proceed when permission is true
                        .collect { isGranted ->
                            if (isGranted) {
                                println("Permission granted, starting audio processing automatically.")
                                viewModel.startAudioProcessing()
                            }
                        }
                }


                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Display permission status or instructions if needed
                        if (!hasPermission) {
                            Text(
                                "Microphone permission needed for audio visualization.",
                                color = Color.Gray // Use a theme color ideally
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // Optionally keep the button to allow re-requesting if denied initially
                            // Button(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                            //     Text("Grant Permission")
                            // }
                        }

                        // Box for the circle - always shown, level controls animation
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f) // Takes up available space
                        ) {
                            AudioCircle(
                                activityLevel = activityLevel,
                                baseSize = 150.dp
                            )
                        }

                        // Removed Start/Stop buttons
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
            // No buttons in preview either
        }
    }
}
