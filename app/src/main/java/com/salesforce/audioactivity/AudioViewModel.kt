package com.salesforce.audioactivity // Use your actual package name

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log // For logging errors/status
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.* // Import Coroutine components
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class AudioViewModel : ViewModel() {

    private val _activityLevel = MutableStateFlow(0.0f)
    val activityLevel = _activityLevel.asStateFlow() // Expose as immutable StateFlow

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted = _permissionGranted.asStateFlow()

    private var audioJob: Job? = null
    private var audioRecord: AudioRecord? = null

    // --- Audio Config ---
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val bufferSize = if (minBufferSize > 0) minBufferSize * 2 else 2048 * 2

    // --- Normalization & Smoothing ---
    // #########################################################################
    // ### IMPORTANT: TUNE THIS VALUE BASED ON LOGCAT RMS OUTPUT ###
    // #########################################################################
    // Lowered significantly to make animation more sensitive and reach max size more often.
    private val maxExpectedRms = 8000.0 // <<<=== ADJUST THIS VALUE BASED ON LOGS & DESIRED SENSITIVITY
    // #########################################################################

    // Increased alpha for faster reaction to loudness changes
    private val alpha = 0.4f // <<<=== ADJUST THIS VALUE (0.0 < alpha <= 1.0)
    private var smoothedLevel = 0.0f

    private val TAG = "AudioViewModel" // For logging

    init {
        Log.d(TAG, "ViewModel Initialized. Min Buffer Size: $minBufferSize, Using Buffer Size: $bufferSize")
        if (minBufferSize <= 0) {
            Log.e(TAG, "AudioRecord.getMinBufferSize failed: $minBufferSize")
        }
    }

    fun updatePermissionStatus(context: Context) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        _permissionGranted.value = hasPermission
        Log.d(TAG, "Permission status updated: $hasPermission")
        if (!hasPermission && audioJob?.isActive == true) {
            stopAudioProcessing()
        }
    }

    @SuppressLint("MissingPermission")
    fun startAudioProcessing() {
        if (!_permissionGranted.value) {
            Log.w(TAG, "StartAudioProcessing called but permission not granted.")
            return
        }
        if (minBufferSize <= 0) {
            Log.e(TAG, "Cannot start audio processing, invalid buffer size.")
            return
        }
        if (audioJob?.isActive == true) {
            // Log.d(TAG, "Audio Processing already running.") // Reduce noise, already started is fine
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed. State: ${audioRecord?.state}")
                audioRecord?.release()
                audioRecord = null
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord creation exception: ${e.message}", e)
            audioRecord = null
            return
        }

        audioJob = viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "Starting Audio Processing Coroutine...")
            val localAudioRecord = audioRecord ?: return@launch

            try {
                val audioBuffer = ShortArray(bufferSize / 2)
                localAudioRecord.startRecording()
                Log.i(TAG, "AudioRecord started recording.")

                var logCounter = 0 // Counter to limit logging frequency

                while (isActive) {
                    val readResult = localAudioRecord.read(audioBuffer, 0, audioBuffer.size)

                    if (readResult > 0) {
                        var sumSquares = 0.0
                        for (i in 0 until readResult) {
                            sumSquares += audioBuffer[i].toDouble() * audioBuffer[i].toDouble()
                        }
                        val rms = sqrt(sumSquares / readResult)
                        val normalized = if (maxExpectedRms > 0) {
                            (rms / maxExpectedRms).toFloat().coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        // Apply smoothing with the adjusted alpha
                        smoothedLevel = alpha * normalized + (1 - alpha) * smoothedLevel
                        _activityLevel.value = smoothedLevel

                        // Log roughly 10 times per second
                        val logFrequencyDivider = (sampleRate.toFloat() / bufferSize / 10f).toInt().coerceAtLeast(1)
                        if (logCounter++ % logFrequencyDivider == 0) {
                            Log.d(TAG, "RMS: ${"%.1f".format(rms)}, Norm: ${"%.3f".format(normalized)}, Smooth: ${"%.3f".format(smoothedLevel)}")
                        }

                    } else if (readResult < 0) {
                        Log.e(TAG, "AudioRecord read error: $readResult")
                        break
                    }
                }

            } catch (e: CancellationException) {
                Log.i(TAG, "Audio processing coroutine cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Audio Processing Exception: ${e.message}", e)
            } finally {
                Log.i(TAG, "Stopping AudioRecord and cleaning up...")
                // Use try-catch for stop/release as they can sometimes throw exceptions
                try {
                    if (localAudioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        localAudioRecord.stop()
                    }
                    localAudioRecord.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during AudioRecord stop/release: ${e.message}", e)
                }

                if (audioRecord == localAudioRecord) {
                    audioRecord = null
                }
                _activityLevel.value = 0.0f
                smoothedLevel = 0.0f
                Log.i(TAG, "Audio processing cleanup complete.")
            }
        }
    }

    fun stopAudioProcessing() {
        if (audioJob?.isActive == true) {
            Log.i(TAG,"Stopping audio processing job...")
            audioJob?.cancel() // Cancellation handled in the finally block of the coroutine
        } else {
            // Log.d(TAG,"Stop called but job not active.") // Reduce noise
        }
        audioJob = null
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel cleared. Stopping audio processing.")
        stopAudioProcessing()
        super.onCleared()
    }
}
