package com.salesforce.audioactivity

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salesforce.audioactivity.ui.theme.cyclingPalette // Import the palette list
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.lerp as lerpColor // Alias lerp for clarity

/**
 * A composable function that displays a circle with a rotating, color-cycling gradient background
 * which scales dramatically based on an activity level using spring physics.
 *
 * @param activityLevel The level of activity (expected 0.0f to 1.0f), controls the pulse scale.
 * @param modifier Modifier for this composable. (Now the first optional parameter)
 * @param baseSize The default diameter of the circle when activityLevel is 0.
 */
@Composable
fun AudioCircle(
    activityLevel: Float,
    modifier: Modifier = Modifier, // Modifier is now the first optional parameter
    baseSize: Dp = 150.dp
) {
    // --- Activity Level Scaling Animation ---
    // Ensure activityLevel is within the expected range [0.0, 1.0]
    val safeLevel = activityLevel.coerceIn(0f, 1f)

    // Define the min and max scale factors for the animation range
    val minScale = 0.8f
    val maxScale = 1.8f

    // Animate the scale factor based on the activity level using a spring
    val scale by animateFloatAsState(
        targetValue = minScale + safeLevel * (maxScale - minScale),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium // Adjusted for responsiveness
        ),
        label = "CircleScale"
    )

    // --- Infinite Transition for Continuous Background Effects ---
    val infiniteTransition = rememberInfiniteTransition(label = "CircleEffects")

    // 1. Gradient Angle Rotation Animation (-15 to +15 degrees)
    val angle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse // Oscillates back and forth
        ),
        label = "GradientAngle"
    )

    // 2. Color Cycling Animation
    val colorCycleDurationPerColor = 2500 // Time spent transitioning towards each color
    val numColors = cyclingPalette.size
    // Animate a continuous float representing the progress through the palette indices
    val colorProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = numColors.toFloat(), // Animate from index 0 to numColors
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = colorCycleDurationPerColor * numColors, easing = LinearEasing),
            repeatMode = RepeatMode.Restart // Loop back to the start index
        ),
        label = "ColorProgress"
    )

    // Calculate the current color index and the next one
    val startIndex = colorProgress.toInt() % numColors
    val nextIndex = (startIndex + 1) % numColors
    // Calculate the fraction (0.0 to 1.0) between the current and next color
    val lerpFraction = colorProgress - colorProgress.toInt()

    // Interpolate smoothly between the current and next start color
    val animatedStartColor = lerpColor( // Use aliased lerp
        cyclingPalette[startIndex],
        cyclingPalette[nextIndex],
        lerpFraction
    )

    // Determine the end color index, offset from the start index
    val endColorOffset = numColors / 3 // Offset within the palette
    val endIndex = (startIndex + endColorOffset) % numColors
    val nextEndIndex = (endIndex + 1) % numColors

    // Interpolate smoothly between the current and next end color using the same fraction
    val animatedEndColor = lerpColor( // Use aliased lerp
        cyclingPalette[endIndex],
        cyclingPalette[nextEndIndex],
        lerpFraction
    )


    // --- Drawing ---
    Box(
        // Pass the modifier parameter here
        modifier = modifier
            .size(baseSize) // Apply the base size
            .scale(scale) // Apply the animated scaling
            .drawBehind { // Custom drawing for the gradient circle
                // Convert animated angle to radians for trig functions
                val angleRad = Math.toRadians(angle.toDouble())
                // Calculate radius based on the current size available to drawBehind
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Calculate start and end points for the linear gradient based on angle
                // These points define the line along which the color transition occurs
                val startOffset = Offset(
                    center.x - (radius * sin(angleRad)).toFloat(),
                    center.y + (radius * cos(angleRad)).toFloat()
                )
                val endOffset = Offset(
                    center.x + (radius * sin(angleRad)).toFloat(),
                    center.y - (radius * cos(angleRad)).toFloat()
                )

                // Create the gradient brush using the animated colors and calculated offsets
                val brush = Brush.linearGradient(
                    colors = listOf(animatedStartColor, animatedEndColor),
                    start = startOffset,
                    end = endOffset
                )

                // Draw the circle shape and fill it with the animated gradient brush
                drawCircle(brush = brush, radius = radius, center = center)
            }
    )
}
