package com.salesforce.audioactivity // Your package name

// --- Essential Imports ---
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.* // Needed for remember, mutableStateOf, LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay // Import delay

// --- Import your App's Theme & The New Composable ---
import com.salesforce.audioactivity.ui.theme.AudioActivityTheme
import com.salesforce.audioactivity.AudioCircle

// --- MainActivity Class ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Force Dark Theme
            AudioActivityTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    // color = MaterialTheme.colorScheme.background // Applied by theme
                ) {
                    // --- Placeholder Animation - More Speech-Like ---
                    var currentLevel by remember { mutableStateOf(0.0f) }

                    LaunchedEffect(Unit) {
                        // Define a sequence of levels and delays to simulate speech
                        val speechPattern = listOf(
                            0.1f to 300L, // Low level, short pause
                            0.6f to 150L, // Medium peak
                            0.9f to 200L, // High peak
                            0.5f to 100L, // Drop
                            0.7f to 180L, // Medium peak again
                            0.3f to 250L, // Lower level
                            0.0f to 400L  // Silence
                        )

                        while (true) { // Loop the pattern
                            for ((level, duration) in speechPattern) {
                                currentLevel = level
                                delay(duration) // Wait for the specified duration
                            }
                        }
                    }
                    // --- End Placeholder Animation ---


                    // Box to center the AudioCircle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Call the AudioCircle, passing the animated level
                        AudioCircle(
                            activityLevel = currentLevel,
                            baseSize = 150.dp // Set the desired base size here
                        )
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Preview the circle, perhaps at a specific level or size
            AudioCircle(activityLevel = 0.5f, baseSize = 150.dp)
        }
    }
}
