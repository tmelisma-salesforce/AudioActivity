package com.salesforce.audioactivity // Your package name

// --- Essential Imports ---
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // Keep this if your template included it
import androidx.compose.foundation.background // Needed for .background()
import androidx.compose.foundation.layout.Box // Needed for Box composable
import androidx.compose.foundation.layout.fillMaxSize // Needed for .fillMaxSize()
import androidx.compose.foundation.layout.size // Needed for .size()
import androidx.compose.foundation.shape.CircleShape // Needed for CircleShape
import androidx.compose.material3.MaterialTheme // Needed for MaterialTheme access
import androidx.compose.material3.Surface // Needed for Surface
import androidx.compose.runtime.Composable // Needed for @Composable annotation
import androidx.compose.ui.Alignment // Needed for Alignment.Center
import androidx.compose.ui.Modifier // Needed for Modifier parameters
// import androidx.compose.ui.graphics.Color // No longer needed directly here for the circle
import androidx.compose.ui.graphics.Brush // *** IMPORT ADDED for Gradient ***
import androidx.compose.ui.tooling.preview.Preview // Needed for @Preview
import androidx.compose.ui.unit.dp // Needed for .dp units

// --- Import your App's Theme & Colors ---
import com.salesforce.audioactivity.ui.theme.AudioActivityTheme
// Import specific colors defined in Color.kt
import com.salesforce.audioactivity.ui.theme.CloudBlue95
import com.salesforce.audioactivity.ui.theme.CloudBlue80
import com.salesforce.audioactivity.ui.theme.SalesforceBlue60

// --- MainActivity Class ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // Keep this if you want edge-to-edge display

        setContent { // Entry point for Compose UI
            // *** Force Dark Theme by setting darkTheme = true ***
            AudioActivityTheme(darkTheme = true) {
                // Surface acts as a root container. Background color comes from the theme.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    // color = MaterialTheme.colorScheme.background // Color is applied by default
                ) {
                    // Box is used here to center its content
                    Box(
                        contentAlignment = Alignment.Center, // Center content
                        modifier = Modifier.fillMaxSize()    // Make the Box fill the screen
                    ) {
                        SimpleCircle() // Display the circle
                    }
                }
            }
        }
    }
}

// --- SimpleCircle Composable Function ---
@Composable
fun SimpleCircle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(100.dp) // Set the size
            // *** Use Brush for Gradient Background ***
            .background(
                brush = Brush.verticalGradient( // Or .horizontalGradient, .radialGradient etc.
                    colors = listOf(
                        CloudBlue80, // Start color (lightest)
                        SalesforceBlue60// End color (lighter)
                    )
                ),
                shape = CircleShape    // Apply the circular shape
            )
    )
    // This Box displays the gradient circle via its background
}

// --- Preview Function ---
// Preview still uses the theme, which will default to its behavior (light/dark based on system)
// unless overridden here too, or if the theme itself is hardcoded.
// The backgroundColor attribute helps visualize it on a dark background in the preview pane.
@Preview(showBackground = true, backgroundColor = 0xFF023248) // Preview with dark background
@Composable
fun CirclePreview() {
    // Use the correct theme name here
    // To force dark in preview too, add darkTheme = true
    AudioActivityTheme(darkTheme = true) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SimpleCircle()
        }
    }
}
