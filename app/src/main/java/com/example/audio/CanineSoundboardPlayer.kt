package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class SoundboardItem(
    val id: String,
    val title: String,
    val canineMeaning: String,
    val humanContext: String,
    val iconEmoji: String,
    val soundCategory: String, // Praise, Command, Play, Care, Calm
    val durationMs: Int = 1200
)

class CanineSoundboardPlayer {
    private val TAG = "SoundboardPlayer"
    private var playbackJob: Job? = null
    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    companion object {
        val SOUNDBOARD_ITEMS = listOf(
            SoundboardItem(
                id = "good_dog",
                title = "Good Dog!",
                canineMeaning = "Praise & Affection",
                humanContext = "Reinforces good behavior with cheerful chirps",
                iconEmoji = "❤️",
                soundCategory = "Praise"
            ),
            SoundboardItem(
                id = "recall_whistle",
                title = "Canine Recall",
                canineMeaning = "Come Here Now!",
                humanContext = "High-penetration canine acoustic whistle",
                iconEmoji = "🦮",
                soundCategory = "Command"
            ),
            SoundboardItem(
                id = "outside_walk",
                title = "Walk Time?",
                canineMeaning = "Outdoors & Adventure",
                humanContext = "Inquisitive rising pitch alerting to leashes",
                iconEmoji = "🌳",
                soundCategory = "Play"
            ),
            SoundboardItem(
                id = "treat_dinner",
                title = "Treat / Dinner",
                canineMeaning = "Food & Rewards!",
                humanContext = "Pleasant harmonic chime + happy puppy trill",
                iconEmoji = "🍖",
                soundCategory = "Care"
            ),
            SoundboardItem(
                id = "play_bow",
                title = "Play Bow!",
                canineMeaning = "Let's Play Together",
                humanContext = "Staccato playful canine solicitation yip",
                iconEmoji = "🎾",
                soundCategory = "Play"
            ),
            SoundboardItem(
                id = "leave_it",
                title = "No / Leave It",
                canineMeaning = "Cease Action Firmly",
                humanContext = "Low authoritative corrective impulse",
                iconEmoji = "🛑",
                soundCategory = "Command"
            ),
            SoundboardItem(
                id = "calm_down",
                title = "Calm Down",
                canineMeaning = "Safety & Relaxation",
                humanContext = "Low-frequency soothing descending purr",
                iconEmoji = "🧘",
                soundCategory = "Calm"
            ),
            SoundboardItem(
                id = "pack_howl",
                title = "Pack Howl",
                canineMeaning = "Where Are You? Together!",
                humanContext = "Resonant melodic pack unity howl",
                iconEmoji = "🐺",
                soundCategory = "Play"
            ),
            SoundboardItem(
                id = "bedtime_settle",
                title = "Bedtime / Settle",
                canineMeaning = "Rest Time in Den",
                humanContext = "Deep delta-wave soothing hum",
                iconEmoji = "💤",
                soundCategory = "Calm"
            )
        )
    }

    fun playSound(item: SoundboardItem, scope: CoroutineScope) {
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.Default) {
            _currentlyPlayingId.value = item.id
            try {
                val sampleRate = 44100
                val samples = generateSyntheticCanineAudio(item.id, sampleRate)
                val bufferSize = samples.size * 2

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                // Wait for playback to finish
                val durationMs = (samples.size * 1000L) / sampleRate
                kotlinx.coroutines.delay(durationMs + 100L)

                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.e(TAG, "Failed playing soundboard audio: ${item.id}", e)
            } finally {
                if (_currentlyPlayingId.value == item.id) {
                    _currentlyPlayingId.value = null
                }
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        _currentlyPlayingId.value = null
    }

    private fun generateSyntheticCanineAudio(id: String, sampleRate: Int): ShortArray {
        return when (id) {
            "good_dog" -> {
                // Ascending cheerful harmonic chirps: 3 pulses from 650Hz up to 1350Hz
                val totalDuration = 1.1
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                val pulseSamples = totalSamples / 3

                for (p in 0 until 3) {
                    val startFreq = 700.0 + (p * 200.0)
                    val endFreq = startFreq + 350.0
                    for (i in 0 until pulseSamples) {
                        val globalIdx = p * pulseSamples + i
                        if (globalIdx >= totalSamples) break
                        val t = i.toDouble() / sampleRate
                        val progress = i.toDouble() / pulseSamples
                        val freq = startFreq + (endFreq - startFreq) * progress
                        // Attack and decay envelope
                        val env = sin(progress * PI)
                        val wave = (sin(2 * PI * freq * t) + 0.3 * sin(4 * PI * freq * t)) * env
                        buffer[globalIdx] = (wave * 26000).toInt().coerceIn(-32767, 32767).toShort()
                    }
                }
                buffer
            }
            "recall_whistle" -> {
                // Canine recall whistle: modulated 4200Hz - 5400Hz frequency with tremolo
                val totalDuration = 1.3
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / totalSamples
                    val baseFreq = 4600.0 + 400.0 * sin(2 * PI * 6.0 * t)
                    val envelope = when {
                        progress < 0.1 -> progress / 0.1
                        progress > 0.85 -> (1.0 - progress) / 0.15
                        else -> 1.0
                    }
                    val wave = (sin(2 * PI * baseFreq * t) + 0.2 * sin(4 * PI * baseFreq * t)) * envelope
                    buffer[i] = (wave * 24000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }
            "outside_walk" -> {
                // Inquisitive two-tone bark/chirp: low then lively high sweep
                val totalDuration = 0.9
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                val mid = totalSamples / 2
                // Tone 1: 420Hz
                for (i in 0 until mid) {
                    val t = i.toDouble() / sampleRate
                    val env = sin((i.toDouble() / mid) * PI)
                    val wave = sin(2 * PI * 440.0 * t) * env
                    buffer[i] = (wave * 28000).toInt().coerceIn(-32767, 32767).toShort()
                }
                // Tone 2: rising sweep 520Hz to 850Hz
                for (i in mid until totalSamples) {
                    val sub = i - mid
                    val t = sub.toDouble() / sampleRate
                    val prog = sub.toDouble() / (totalSamples - mid)
                    val freq = 520.0 + 330.0 * prog
                    val env = sin(prog * PI)
                    val wave = sin(2 * PI * freq * t) * env
                    buffer[i] = (wave * 28000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }
            "treat_dinner" -> {
                // Pleasant dinner bell chime (980Hz + 1960Hz decaying) + happy pup squeak
                val totalDuration = 1.2
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val chime = (sin(2 * PI * 1174.0 * t) + 0.5 * sin(2 * PI * 2349.0 * t)) * exp(-3.5 * t)
                    val squeakTime = t - 0.45
                    val squeak = if (squeakTime > 0) {
                        sin(2 * PI * 850.0 * squeakTime) * exp(-6.0 * squeakTime)
                    } else 0.0
                    val combined = (chime * 0.7 + squeak * 0.5)
                    buffer[i] = (combined * 28000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }
            "play_bow" -> {
                // Playful double yip: two fast 550Hz staccato bursts
                val totalDuration = 0.8
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                val half = totalSamples / 2
                for (burst in 0..1) {
                    val start = burst * half
                    val burstLen = half - (sampleRate * 0.05).toInt()
                    for (i in 0 until burstLen) {
                        val t = i.toDouble() / sampleRate
                        val prog = i.toDouble() / burstLen
                        val freq = 520.0 + 80.0 * sin(prog * PI)
                        val env = sin(prog * PI)
                        val wave = (sin(2 * PI * freq * t) + 0.4 * sin(4 * PI * freq * t)) * env
                        buffer[start + i] = (wave * 29000).toInt().coerceIn(-32767, 32767).toShort()
                    }
                }
                buffer
            }
            "leave_it" -> {
                // Firm low-frequency command tone (160Hz with rich harmonic bite)
                val totalDuration = 0.6
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val prog = i.toDouble() / totalSamples
                    val env = (1.0 - prog) * (1.0 - prog)
                    val wave = (sin(2 * PI * 160.0 * t) + 0.6 * sin(2 * PI * 320.0 * t) + 0.3 * sin(2 * PI * 480.0 * t)) * env
                    buffer[i] = (wave * 30000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }
            "calm_down" -> {
                // Soothing descending gentle tone & warm hum: 340Hz down to 180Hz
                val totalDuration = 1.4
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val prog = i.toDouble() / totalSamples
                    val freq = 340.0 - (160.0 * prog)
                    val env = sin(prog * PI)
                    val wave = sin(2 * PI * freq * t) * env
                    buffer[i] = (wave * 22000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }
            "pack_howl" -> {
                // Pack howl sweep: starts low (260Hz), glides up to (540Hz), sustains with vibrato, then tapers off
                val totalDuration = 2.0
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val prog = i.toDouble() / totalSamples
                    val freq = when {
                        prog < 0.3 -> 280.0 + (prog / 0.3) * 260.0
                        prog < 0.7 -> 540.0 + 20.0 * sin(2 * PI * 4.0 * t)
                        else -> 540.0 - ((prog - 0.7) / 0.3) * 180.0
                    }
                    val env = sin(prog * PI)
                    val wave = (sin(2 * PI * freq * t) + 0.35 * sin(4 * PI * freq * t) + 0.15 * sin(6 * PI * freq * t)) * env
                    buffer[i] = (wave * 27000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }
            else -> {
                // Bedtime Settle: 110Hz deep soothing theta hum
                val totalDuration = 1.5
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val prog = i.toDouble() / totalSamples
                    val env = sin(prog * PI)
                    val wave = (sin(2 * PI * 110.0 * t) + 0.25 * sin(2 * PI * 220.0 * t)) * env
                    buffer[i] = (wave * 20000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }
        }
    }
}
