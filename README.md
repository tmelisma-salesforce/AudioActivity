# Audio Activity Visualizer

## Description

This is an Android application built with Jetpack Compose that visualizes audio input from the device microphone. It displays an animated circle whose appearance changes based on the detected audio level.

Specifically, the app:
* Captures audio from the microphone using `AudioRecord`.
* Calculates the Root Mean Square (RMS) value of the audio buffer to determine loudness.
* Normalizes and smooths the RMS value to produce an `activityLevel` (0.0 to 1.0).
* Displays a circle (`AudioCircle` composable) that:
    * Scales its size based on the `activityLevel`.
    * Features a background gradient that continuously rotates.
    * Features gradient colors that continuously cycle through a predefined palette of Salesforce brand colors.
* Automatically starts listening when the app is opened (permission permitting) and stops when paused or closed.

## Program Structure

The application follows a basic MVVM (Model-View-ViewModel) pattern using Jetpack Compose:

* **`MainActivity.kt`**:
    * The main entry point for the application.
    * Sets up the Jetpack Compose UI (`setContent`).
    * Handles the runtime request for the `RECORD_AUDIO` permission.
    * Observes state (`activityLevel`, `permissionGranted`) from the `AudioViewModel`.
    * Hosts the `AudioCircle` composable.
    * Manages starting/stopping audio processing based on lifecycle events and permission status.
* **`AudioViewModel.kt`**:
    * A `ViewModel` responsible for the audio processing logic.
    * Initializes and manages the `AudioRecord` instance.
    * Runs audio capture and processing on a background coroutine (`viewModelScope`).
    * Calculates RMS, normalizes it against `maxExpectedRms`, and applies smoothing (`alpha`).
    * Exposes the final `activityLevel` and `permissionGranted` status as `StateFlow` for the UI to observe.
    * Handles starting and stopping the audio processing job and releasing resources.
* **`AudioCircle.kt`**:
    * A reusable `@Composable` function that displays the visualization.
    * Takes `activityLevel` and `baseSize` as parameters.
    * Uses `animateFloatAsState` with a `spring` spec to animate its scale based on `activityLevel`.
    * Uses `rememberInfiniteTransition` to create continuous animations for gradient rotation and color cycling.
    * Uses `Modifier.drawBehind` to draw the circle with the complex, animated gradient background.
* **`ui/theme/`**: Contains standard Jetpack Compose theme files:
    * `Color.kt`: Defines the color palettes used in the app (including the cycling palette). Based on Salesforce brand colors.
    * `Theme.kt`: Sets up the `MaterialTheme`, defining light/dark color schemes (currently forced dark) and applying typography.
    * `Type.kt`: Defines typography styles (likely default).
* **`AndroidManifest.xml`**: Declares the necessary `RECORD_AUDIO` permission.

## Copyright

Copyright 2025 Toni Melisma

