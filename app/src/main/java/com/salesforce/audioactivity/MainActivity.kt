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

// --- Import your App's Theme & The AudioCircle Composable ---
import com.salesforce.audioactivity.ui.theme.AudioActivityTheme
import com.salesforce.audioactivity.AudioCircle // Should match the file containing AudioCircle

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
                    // --- Placeholder Animation - ~10 Second Spoken Paragraph Simulation ---
                    var currentLevel by remember { mutableStateOf(0.0f) }

                    LaunchedEffect(Unit) {
                        // Sequence of (level, duration in ms) pairs
                        // Aiming for > 10,000 ms total duration
                        val paragraphPattern = listOf(
                            // Sentence 1: "This is the first sentence."
                            0.1f to 150L,  // Start low
                            0.6f to 120L,  // "This"
                            0.3f to 80L,   // "is"
                            0.1f to 50L,
                            0.7f to 150L,  // "the"
                            0.8f to 180L,  // "first"
                            0.4f to 100L,
                            0.9f to 200L,  // "sen"
                            0.6f to 150L,  // "tence."
                            0.2f to 300L,  // Pause after sentence 1

                            // Sentence 2: "It has several peaks and valleys."
                            0.5f to 100L,  // "It"
                            0.7f to 130L,  // "has"
                            0.4f to 90L,
                            0.8f to 160L,  // "seve"
                            0.5f to 110L,  // "ral"
                            0.2f to 100L,
                            0.9f to 180L,  // "peaks"
                            0.3f to 80L,
                            0.6f to 120L,  // "and"
                            0.4f to 90L,
                            0.8f to 170L,  // "val"
                            0.5f to 140L,  // "leys."
                            0.1f to 400L,  // Pause after sentence 2

                            // Sentence 3: "Simulating speech rhythm is tricky."
                            0.7f to 150L,  // "Simu"
                            0.9f to 190L,  // "lating"
                            0.5f to 120L,
                            0.8f to 160L,  // "speech"
                            0.6f to 140L,
                            0.9f to 180L,  // "rhy"
                            0.7f to 150L,  // "thm"
                            0.4f to 100L,
                            0.7f to 130L,  // "is"
                            0.3f to 90L,
                            0.8f to 170L,  // "tric"
                            0.6f to 150L,  // "ky."
                            0.1f to 500L,  // Longer pause

                            // Sentence 4: "Finally, a bit more silence."
                            0.5f to 120L,  // "Fi"
                            0.7f to 150L,  // "nal"
                            0.4f to 110L,  // "ly,"
                            0.2f to 200L,  // pause
                            0.6f to 130L,  // "a"
                            0.7f to 140L,  // "bit"
                            0.5f to 100L,
                            0.8f to 160L,  // "more"
                            0.3f to 120L,
                            0.9f to 190L,  // "si"
                            0.6f to 160L,  // "lence."
                            0.0f to 1500L // Long silence at the end (~10.7 seconds total)
                        )

                        // Calculate total duration for verification (optional)
                        val totalDuration = paragraphPattern.sumOf { it.second }
                        println("Placeholder animation cycle duration: $totalDuration ms")

                        while (true) { // Loop the pattern
                            for ((level, duration) in paragraphPattern) {
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
                        // Make sure AudioCircle composable is defined (likely in another file now)
                        AudioCircle(
                            activityLevel = currentLevel,
                            baseSize = 150.dp // Consistent base size
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
