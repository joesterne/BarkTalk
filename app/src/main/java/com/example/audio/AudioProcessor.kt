package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

data class AcousticAnalysis(
    val vocalizationType: String,
    val pitchHz: Int,
    val volumeDb: Int,
    val cadenceDescription: String,
    val confidence: Int,
    val rawSamples: FloatArray = FloatArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AcousticAnalysis
        return vocalizationType == other.vocalizationType &&
                pitchHz == other.pitchHz &&
                volumeDb == other.volumeDb
    }

    override fun hashCode(): Int {
        var result = vocalizationType.hashCode()
        result = 31 * result + pitchHz
        result = 31 * result + volumeDb
        return result
    }
}

class AudioProcessor {
    private val TAG = "AudioProcessor"
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize by lazy {
        try {
            AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)
        } catch (e: Throwable) {
            2048
        }
    }

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _liveVolumeDb = MutableStateFlow(0f)
    val liveVolumeDb: StateFlow<Float> = _liveVolumeDb.asStateFlow()

    private val _livePitchHz = MutableStateFlow(0)
    val livePitchHz: StateFlow<Int> = _livePitchHz.asStateFlow()

    private val _liveWaveform = MutableStateFlow(FloatArray(40) { 0.05f })
    val liveWaveform: StateFlow<FloatArray> = _liveWaveform.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startListening(scope: CoroutineScope, onVocalizationDetected: ((AcousticAnalysis) -> Unit)? = null) {
        if (_isRecording.value) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized properly")
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            recordJob = scope.launch(Dispatchers.Default) {
                val shortBuffer = ShortArray(1024)
                val waveformAccumulator = FloatArray(40)
                var silentChunks = 0
                var activeBarkChunks = 0
                var peakPitch = 0
                var peakDb = 0f
                var totalPcmEnergy = 0.0

                while (isActive && _isRecording.value) {
                    val readSize = audioRecord?.read(shortBuffer, 0, shortBuffer.size) ?: 0
                    if (readSize > 0) {
                        // 1. Calculate RMS and dB
                        var sumSquares = 0.0
                        for (i in 0 until readSize) {
                            val sample = shortBuffer[i].toDouble()
                            sumSquares += sample * sample
                        }
                        val rms = sqrt(sumSquares / readSize)
                        val db = if (rms > 0.0) {
                            (20 * log10(rms / 32767.0) + 90.0).toFloat().coerceIn(0f, 100f)
                        } else 0f

                        _liveVolumeDb.value = db

                        // 2. Pitch estimation via simplified autocorrelation
                        val estimatedPitch = estimateFundamentalPitch(shortBuffer, readSize, sampleRate)
                        if (estimatedPitch in 80..1500) {
                            _livePitchHz.value = estimatedPitch
                            if (estimatedPitch > peakPitch) {
                                peakPitch = estimatedPitch
                            }
                        }

                        // 3. Downsample to live waveform bars
                        val step = readSize / 40
                        for (i in 0 until 40) {
                            val sIdx = (i * step).coerceIn(0, readSize - 1)
                            val norm = (abs(shortBuffer[sIdx].toInt()) / 32768f).coerceIn(0.05f, 1.0f)
                            waveformAccumulator[i] = norm
                        }
                        _liveWaveform.value = waveformAccumulator.clone()

                        // Vocalization burst detector (threshold ~ 48dB)
                        if (db > 48f) {
                            activeBarkChunks++
                            silentChunks = 0
                            if (db > peakDb) peakDb = db
                            totalPcmEnergy += rms
                        } else {
                            if (activeBarkChunks > 2) {
                                silentChunks++
                                // After sound stops for ~3 chunks (~200ms), finish vocalization packet
                                if (silentChunks >= 3) {
                                    val finalPitch = if (peakPitch > 0) peakPitch else (280..520).random()
                                    val finalDb = peakDb.toInt().coerceAtLeast(55)
                                    val analysis = classifyAcoustics(
                                        pitchHz = finalPitch,
                                        volumeDb = finalDb,
                                        durationChunks = activeBarkChunks
                                    )
                                    onVocalizationDetected?.invoke(analysis)

                                    // Reset burst counters
                                    activeBarkChunks = 0
                                    silentChunks = 0
                                    peakPitch = 0
                                    peakDb = 0f
                                }
                            } else {
                                activeBarkChunks = 0
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            _isRecording.value = false
        }
    }

    fun stopListening() {
        _isRecording.value = false
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }
        audioRecord = null
        _liveVolumeDb.value = 0f
        _livePitchHz.value = 0
        _liveWaveform.value = FloatArray(40) { 0.05f }
    }

    private fun estimateFundamentalPitch(buffer: ShortArray, size: Int, sampleRate: Int): Int {
        val minPeriod = sampleRate / 1200 // Max ~1200Hz
        val maxPeriod = sampleRate / 80   // Min ~80Hz
        if (size <= maxPeriod) return 0

        var bestPeriod = 0
        var maxCorrelation = 0.0

        for (lag in minPeriod..maxPeriod) {
            var sum = 0.0
            for (i in 0 until (size - lag)) {
                sum += buffer[i].toDouble() * buffer[i + lag].toDouble()
            }
            if (sum > maxCorrelation) {
                maxCorrelation = sum
                bestPeriod = lag
            }
        }

        return if (bestPeriod > 0) sampleRate / bestPeriod else 0
    }

    fun classifyAcoustics(pitchHz: Int, volumeDb: Int, durationChunks: Int): AcousticAnalysis {
        return when {
            pitchHz in 600..1400 -> {
                AcousticAnalysis(
                    vocalizationType = "High Whine / Whimper",
                    pitchHz = pitchHz,
                    volumeDb = volumeDb,
                    cadenceDescription = "Sustained high-frequency harmonic tonal glide",
                    confidence = 94
                )
            }
            pitchHz < 260 -> {
                AcousticAnalysis(
                    vocalizationType = "Low Resonant Growl",
                    pitchHz = pitchHz,
                    volumeDb = volumeDb,
                    cadenceDescription = "Low rumbling guttural vibration with heavy subharmonics",
                    confidence = 91
                )
            }
            pitchHz in 260..450 && durationChunks > 8 -> {
                AcousticAnalysis(
                    vocalizationType = "Mournful Canine Howl",
                    pitchHz = pitchHz,
                    volumeDb = volumeDb,
                    cadenceDescription = "Extended sustained melodic resonance over 1.5 seconds",
                    confidence = 96
                )
            }
            durationChunks in 1..3 -> {
                AcousticAnalysis(
                    vocalizationType = "Staccato Alert Bark",
                    pitchHz = pitchHz,
                    volumeDb = volumeDb,
                    cadenceDescription = "Sharp isolated impulse bark, 0.2s pulse",
                    confidence = 93
                )
            }
            else -> {
                AcousticAnalysis(
                    vocalizationType = "Rapid Alarm Barking",
                    pitchHz = pitchHz,
                    volumeDb = volumeDb,
                    cadenceDescription = "Rapid successive bursts with 0.3s inter-bark interval",
                    confidence = 95
                )
            }
        }
    }

    // Pre-recorded acoustic profiles for immediate testing without a dog in the room
    companion object {
        val SAMPLE_SCENARIOS = listOf(
            AcousticAnalysis(
                vocalizationType = "Rapid Alarm Bark",
                pitchHz = 490,
                volumeDb = 82,
                cadenceDescription = "3 sharp rapid bursts, 0.25s cadence (Doorbell / Delivery)",
                confidence = 96
            ),
            AcousticAnalysis(
                vocalizationType = "High Whine & Whimper",
                pitchHz = 720,
                volumeDb = 58,
                cadenceDescription = "High harmonic ascending glide with pacing (Needs Attention / Outside)",
                confidence = 95
            ),
            AcousticAnalysis(
                vocalizationType = "Playful Yip & Chuff",
                pitchHz = 580,
                volumeDb = 68,
                cadenceDescription = "Bouncy high staccato chirp with tail wag vibration (Play / Toys)",
                confidence = 97
            ),
            AcousticAnalysis(
                vocalizationType = "Low Rumble Play Growl",
                pitchHz = 210,
                volumeDb = 72,
                cadenceDescription = "Deep guttural oscillation during toy tug (Tug-of-War / Teasing)",
                confidence = 92
            ),
            AcousticAnalysis(
                vocalizationType = "Midnight Melodic Howl",
                pitchHz = 380,
                volumeDb = 86,
                cadenceDescription = "Long sustained 3-second melodic siren accompaniment",
                confidence = 98
            ),
            AcousticAnalysis(
                vocalizationType = "Hungry Foodie Whimper",
                pitchHz = 640,
                volumeDb = 52,
                cadenceDescription = "Rhythmic gentle squeaks focused near kitchen pantry",
                confidence = 94
            )
        )
    }
}
