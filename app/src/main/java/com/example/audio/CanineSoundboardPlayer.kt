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
import kotlin.math.pow
import kotlin.math.sin

data class SoundboardItem(
    val id: String,
    val title: String,
    val canineMeaning: String,
    val humanContext: String,
    val iconEmoji: String,
    val emotion: String = "Happy", // Happy, Playful, Warning, Alert, Curious, Calm
    val soundCategory: String = emotion,
    val dominantPitchHz: Int = 520,
    val acousticType: String = "Synthesized Bark",
    val durationMs: Int = 1200
) {
    // Secondary constructor to ensure 100% backward compatibility
    constructor(
        id: String,
        title: String,
        canineMeaning: String,
        humanContext: String,
        iconEmoji: String,
        soundCategory: String,
        durationMs: Int = 1200
    ) : this(
        id = id,
        title = title,
        canineMeaning = canineMeaning,
        humanContext = humanContext,
        iconEmoji = iconEmoji,
        emotion = when (soundCategory.lowercase()) {
            "praise" -> "Happy"
            "command" -> "Warning"
            "play" -> "Playful"
            "care" -> "Happy"
            "calm" -> "Calm"
            else -> "Happy"
        },
        soundCategory = soundCategory,
        dominantPitchHz = 520,
        acousticType = "Synthesized Bark",
        durationMs = durationMs
    )
}

class CanineSoundboardPlayer {
    private val TAG = "SoundboardPlayer"
    private var playbackJob: Job? = null
    private val preRecordedSampleCache = java.util.concurrent.ConcurrentHashMap<String, ShortArray>()

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _lastPlayedSound = MutableStateFlow<SoundboardItem?>(null)
    val lastPlayedSound: StateFlow<SoundboardItem?> = _lastPlayedSound.asStateFlow()

    companion object {
        val SOUNDBOARD_ITEMS = listOf(
            // === HAPPY EMOTION SYNTHESIZED BARKS ===
            SoundboardItem(
                id = "happy_greeting",
                title = "Happy Greeting Bark",
                canineMeaning = "Joyful Welcome & Tail Wag",
                humanContext = "Upbeat double-bark with bouncy harmonics to greet your dog",
                iconEmoji = "🐶",
                emotion = "Happy",
                dominantPitchHz = 540,
                acousticType = "Synthesized Double Bark",
                durationMs = 1100
            ),
            SoundboardItem(
                id = "happy_yip",
                title = "Joyful Pup Yip",
                canineMeaning = "Excitement & Affection",
                humanContext = "High-frequency cheerful staccato yips for praise and bonding",
                iconEmoji = "❤️",
                emotion = "Happy",
                dominantPitchHz = 680,
                acousticType = "Synthesized Staccato Yip",
                durationMs = 950
            ),
            SoundboardItem(
                id = "happy_welcome",
                title = "Excited Reunion Woof",
                canineMeaning = "So Glad You're Home!",
                humanContext = "Harmonic warm double woof celebrating human reunion",
                iconEmoji = "✨",
                emotion = "Happy",
                dominantPitchHz = 490,
                acousticType = "Synthesized Warm Woof",
                durationMs = 1200
            ),

            // === PLAYFUL EMOTION SYNTHESIZED BARKS ===
            SoundboardItem(
                id = "playful_bow",
                title = "Play-Bow Bark",
                canineMeaning = "Let's Play Together!",
                humanContext = "Staccato invitation bark mimicking the universal canine play bow",
                iconEmoji = "🎾",
                emotion = "Playful",
                dominantPitchHz = 590,
                acousticType = "Synthesized Solicitation Bark",
                durationMs = 1000
            ),
            SoundboardItem(
                id = "playful_fetch",
                title = "Bouncy Fetch Bark",
                canineMeaning = "Throw the Ball / Chase!",
                humanContext = "High-energy triple pulse signaling active game mode",
                iconEmoji = "🥏",
                emotion = "Playful",
                dominantPitchHz = 620,
                acousticType = "Synthesized Triple Yip",
                durationMs = 1150
            ),
            SoundboardItem(
                id = "playful_chuff",
                title = "Teasing Tag Chuff",
                canineMeaning = "Come Catch Me!",
                humanContext = "Bouncy chuff-woof with rising pitch used during chase games",
                iconEmoji = "🐾",
                emotion = "Playful",
                dominantPitchHz = 510,
                acousticType = "Synthesized Chuff-Woof",
                durationMs = 900
            ),

            // === WARNING EMOTION SYNTHESIZED BARKS ===
            SoundboardItem(
                id = "warning_guard",
                title = "Territorial Guard Bark",
                canineMeaning = "Stay Back / Boundary Alert",
                humanContext = "Deep authoritative 220Hz chest bark with resonant low harmonics",
                iconEmoji = "🛡️",
                emotion = "Warning",
                dominantPitchHz = 220,
                acousticType = "Synthesized Guard Bark",
                durationMs = 1100
            ),
            SoundboardItem(
                id = "warning_perimeter",
                title = "Perimeter Warning Woof",
                canineMeaning = "Intruder at Edge / Watch Out",
                humanContext = "Explosive single warning impulse asserting boundary defense",
                iconEmoji = "⚠️",
                emotion = "Warning",
                dominantPitchHz = 260,
                acousticType = "Synthesized Defensive Woof",
                durationMs = 850
            ),
            SoundboardItem(
                id = "warning_growl",
                title = "Low Threat Growl-Bark",
                canineMeaning = "Cease Action Firmly!",
                humanContext = "Guttural 170Hz chest rumble culminating in an assertive warning snap",
                iconEmoji = "🛑",
                emotion = "Warning",
                dominantPitchHz = 175,
                acousticType = "Synthesized Growl-Bark",
                durationMs = 1300
            ),

            // === ALERT EMOTION SYNTHESIZED BARKS ===
            SoundboardItem(
                id = "alert_sentinel",
                title = "Sentinel Attention Bark",
                canineMeaning = "Focus Here / Look!",
                humanContext = "High-clarity single 470Hz ping commanding immediate pack focus",
                iconEmoji = "🚨",
                emotion = "Alert",
                dominantPitchHz = 470,
                acousticType = "Synthesized Sentinel Bark",
                durationMs = 800
            ),
            SoundboardItem(
                id = "alert_doorbell",
                title = "Arrival Alert Bark",
                canineMeaning = "Someone Is Coming!",
                humanContext = "Double inquiring bark pulse alerting to doorbell or visitor",
                iconEmoji = "🔔",
                emotion = "Alert",
                dominantPitchHz = 510,
                acousticType = "Synthesized Arrival Bark",
                durationMs = 1050
            ),

            // === CURIOUS EMOTION SYNTHESIZED BARKS ===
            SoundboardItem(
                id = "curious_headtilt",
                title = "Head-Tilt Question Woof",
                canineMeaning = "What's That? Tell Me!",
                humanContext = "Inquisitive upward pitch glide imitating an inquiring head tilt",
                iconEmoji = "🧐",
                emotion = "Curious",
                dominantPitchHz = 420,
                acousticType = "Synthesized Inquiring Sweep",
                durationMs = 950
            ),

            // === CALM EMOTION SYNTHESIZED BARKS ===
            SoundboardItem(
                id = "calm_settle",
                title = "Denning Settle Woof",
                canineMeaning = "All Safe in Den / Rest",
                humanContext = "Deep soothing 180Hz soft breath reassuring security and calm",
                iconEmoji = "💤",
                emotion = "Calm",
                dominantPitchHz = 180,
                acousticType = "Synthesized Soothing Woof",
                durationMs = 1400
            ),

            // === LEGACY UTILITY FAVORITES ===
            SoundboardItem(
                id = "good_dog",
                title = "Good Dog Praise",
                canineMeaning = "Affection & Praise",
                humanContext = "Harmonic canine chirps reinforcing positive behavior",
                iconEmoji = "❤️",
                emotion = "Happy",
                dominantPitchHz = 750,
                acousticType = "Synthesized Praise Chirp",
                durationMs = 1100
            ),
            SoundboardItem(
                id = "recall_whistle",
                title = "Canine Recall Whistle",
                canineMeaning = "Come Here Now!",
                humanContext = "High-penetration ultrasonic acoustic whistle",
                iconEmoji = "🦮",
                emotion = "Alert",
                dominantPitchHz = 4600,
                acousticType = "Synthesized Ultrasonic Whistle",
                durationMs = 1300
            ),
            SoundboardItem(
                id = "outside_walk",
                title = "Walk Time Inquiry",
                canineMeaning = "Outdoors & Adventure",
                humanContext = "Inquisitive rising pitch alerting to leashes and walk time",
                iconEmoji = "🌳",
                emotion = "Playful",
                dominantPitchHz = 520,
                acousticType = "Synthesized Adventure Bark",
                durationMs = 900
            ),
            SoundboardItem(
                id = "treat_dinner",
                title = "Treat & Dinner Chime",
                canineMeaning = "Food & Rewards!",
                humanContext = "Pleasant harmonic chime with happy pup trill",
                iconEmoji = "🍖",
                emotion = "Happy",
                dominantPitchHz = 1170,
                acousticType = "Harmonic Reward Chime",
                durationMs = 1200
            ),
            SoundboardItem(
                id = "pack_howl",
                title = "Pack Unity Howl",
                canineMeaning = "Where Are You? Together!",
                humanContext = "Melodic resonant pack howl strengthening companion bond",
                iconEmoji = "🐺",
                emotion = "Playful",
                dominantPitchHz = 540,
                acousticType = "Synthesized Canine Howl",
                durationMs = 2000
            )
        )
    }

    fun getPreRecordedPcmSamples(id: String, sampleRate: Int = 44100): ShortArray {
        return preRecordedSampleCache.getOrPut(id) {
            generateSyntheticCanineAudio(id, sampleRate)
        }
    }

    fun playSound(item: SoundboardItem, scope: CoroutineScope) {
        playbackJob?.cancel()
        _currentlyPlayingId.value = item.id
        _lastPlayedSound.value = item
        playbackJob = scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val samples = getPreRecordedPcmSamples(item.id, sampleRate)
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

    /**
     * Synthesizes realistic canine bark vocalizations using acoustic formant modeling,
     * downward pitch glide, harmonic overtone stacking, and aspiration breath noise.
     */
    private fun synthesizeBarkPulse(
        sampleRate: Int,
        fundamentalHz: Double,
        durationSec: Double,
        pitchDroopHz: Double = 60.0,
        harmonics: DoubleArray = doubleArrayOf(1.0, 0.6, 0.35, 0.15),
        breathNoiseRatio: Double = 0.22,
        seed: Long = 12345L
    ): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        var noiseState = seed

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples

            // Canine pitch envelope: downward glide over the duration
            val freq = (fundamentalHz - pitchDroopHz * progress.pow(0.65)).coerceAtLeast(80.0)

            phase += 2.0 * PI * freq / sampleRate
            if (phase > 2.0 * PI) phase -= 2.0 * PI

            // Stack harmonics with formant structure
            var tonalWave = 0.0
            for ((idx, weight) in harmonics.withIndex()) {
                val h = idx + 1
                tonalWave += weight * sin(phase * h)
            }

            // Aspiration breath noise (gives authentic bark puff)
            noiseState = (noiseState * 1103515245L + 12345L) and 0x7FFFFFFF
            val whiteNoise = (noiseState.toDouble() / 0x7FFFFFFF) * 2.0 - 1.0
            val noiseMod = whiteNoise * breathNoiseRatio

            // Canine bark envelope: sharp 18ms attack, rounded crest, natural decay
            val attackTime = 0.02
            val envelope = when {
                t < attackTime -> (t / attackTime) * (t / attackTime)
                else -> {
                    val decayProg = (t - attackTime) / (durationSec - attackTime)
                    exp(-4.2 * decayProg)
                }
            }

            val combined = (tonalWave * (1.0 - breathNoiseRatio) + noiseMod) * envelope
            buffer[i] = (combined * 30000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return buffer
    }

    private fun concatenatePulses(sampleRate: Int, pulses: List<ShortArray>, pauseSec: Double = 0.09): ShortArray {
        val pauseSamples = (sampleRate * pauseSec).toInt()
        val totalLength = pulses.sumOf { it.size } + (pulses.size - 1).coerceAtLeast(0) * pauseSamples
        val result = ShortArray(totalLength)
        var offset = 0
        for ((index, pulse) in pulses.withIndex()) {
            System.arraycopy(pulse, 0, result, offset, pulse.size)
            offset += pulse.size
            if (index < pulses.size - 1) {
                offset += pauseSamples
            }
        }
        return result
    }

    private fun generateSyntheticCanineAudio(id: String, sampleRate: Int): ShortArray {
        return when (id) {
            // === HAPPY EMOTION SYNTHESIZED BARKS ===
            "happy_greeting" -> {
                // Cheerful double bark: 540Hz then 590Hz with light harmonics
                val pulse1 = synthesizeBarkPulse(sampleRate, 540.0, 0.28, pitchDroopHz = 40.0, harmonics = doubleArrayOf(1.0, 0.5, 0.25))
                val pulse2 = synthesizeBarkPulse(sampleRate, 590.0, 0.32, pitchDroopHz = 50.0, harmonics = doubleArrayOf(1.0, 0.55, 0.3))
                concatenatePulses(sampleRate, listOf(pulse1, pulse2), pauseSec = 0.10)
            }
            "happy_yip" -> {
                // High joyful puppy yips: 3 rapid 680Hz pulses
                val p1 = synthesizeBarkPulse(sampleRate, 680.0, 0.16, pitchDroopHz = 30.0, breathNoiseRatio = 0.15)
                val p2 = synthesizeBarkPulse(sampleRate, 710.0, 0.17, pitchDroopHz = 35.0, breathNoiseRatio = 0.15)
                val p3 = synthesizeBarkPulse(sampleRate, 670.0, 0.20, pitchDroopHz = 40.0, breathNoiseRatio = 0.15)
                concatenatePulses(sampleRate, listOf(p1, p2, p3), pauseSec = 0.07)
            }
            "happy_welcome" -> {
                // Warm resonant reunion woof: 480Hz double woof
                val p1 = synthesizeBarkPulse(sampleRate, 480.0, 0.35, pitchDroopHz = 50.0, harmonics = doubleArrayOf(1.0, 0.7, 0.4, 0.2))
                val p2 = synthesizeBarkPulse(sampleRate, 510.0, 0.38, pitchDroopHz = 55.0, harmonics = doubleArrayOf(1.0, 0.65, 0.35))
                concatenatePulses(sampleRate, listOf(p1, p2), pauseSec = 0.12)
            }

            // === PLAYFUL EMOTION SYNTHESIZED BARKS ===
            "playful_bow" -> {
                // Play-bow solicitation bark: 590Hz followed by panting acoustic breath
                val bark = synthesizeBarkPulse(sampleRate, 590.0, 0.30, pitchDroopHz = 60.0, breathNoiseRatio = 0.25)
                val pant = synthesizeBarkPulse(sampleRate, 440.0, 0.22, pitchDroopHz = 30.0, breathNoiseRatio = 0.55)
                concatenatePulses(sampleRate, listOf(bark, pant), pauseSec = 0.08)
            }
            "playful_fetch" -> {
                // Bouncy fetch bark: rapid 620Hz triple pulse
                val p1 = synthesizeBarkPulse(sampleRate, 620.0, 0.20, pitchDroopHz = 35.0)
                val p2 = synthesizeBarkPulse(sampleRate, 640.0, 0.22, pitchDroopHz = 40.0)
                val p3 = synthesizeBarkPulse(sampleRate, 610.0, 0.25, pitchDroopHz = 45.0)
                concatenatePulses(sampleRate, listOf(p1, p2, p3), pauseSec = 0.07)
            }
            "playful_chuff" -> {
                // Teasing tag chuff: 460Hz to 620Hz rising sweep
                val totalDuration = 0.6
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val prog = i.toDouble() / totalSamples
                    val freq = 460.0 + 160.0 * prog
                    val env = sin(prog * PI) * exp(-2.0 * prog)
                    val wave = (sin(2 * PI * freq * t) + 0.4 * sin(4 * PI * freq * t)) * env
                    buffer[i] = (wave * 28000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }

            // === WARNING EMOTION SYNTHESIZED BARKS ===
            "warning_guard" -> {
                // Authoritative territorial guard bark: deep 215Hz fundamental with rich resonant 430Hz harmonics
                val pulse1 = synthesizeBarkPulse(
                    sampleRate = sampleRate,
                    fundamentalHz = 215.0,
                    durationSec = 0.38,
                    pitchDroopHz = 35.0,
                    harmonics = doubleArrayOf(1.0, 0.85, 0.6, 0.3, 0.15),
                    breathNoiseRatio = 0.18
                )
                val pulse2 = synthesizeBarkPulse(
                    sampleRate = sampleRate,
                    fundamentalHz = 210.0,
                    durationSec = 0.42,
                    pitchDroopHz = 40.0,
                    harmonics = doubleArrayOf(1.0, 0.9, 0.65, 0.35, 0.2),
                    breathNoiseRatio = 0.20
                )
                concatenatePulses(sampleRate, listOf(pulse1, pulse2), pauseSec = 0.11)
            }
            "warning_perimeter" -> {
                // Single explosive perimeter warning bark: 255Hz sharp impulse
                synthesizeBarkPulse(
                    sampleRate = sampleRate,
                    fundamentalHz = 255.0,
                    durationSec = 0.40,
                    pitchDroopHz = 50.0,
                    harmonics = doubleArrayOf(1.0, 0.75, 0.5, 0.25),
                    breathNoiseRatio = 0.25
                )
            }
            "warning_growl" -> {
                // Guttural 165Hz chest growl transitioning into warning snap
                val growlSamples = (sampleRate * 0.45).toInt()
                val growlBuffer = ShortArray(growlSamples)
                for (i in 0 until growlSamples) {
                    val t = i.toDouble() / sampleRate
                    val prog = i.toDouble() / growlSamples
                    val freq = 165.0 + 15.0 * sin(2 * PI * 18.0 * t) // Guttural modulation
                    val env = sin(prog * PI * 0.5)
                    val wave = (sin(2 * PI * freq * t) + 0.7 * sin(4 * PI * freq * t) + 0.4 * sin(6 * PI * freq * t)) * env
                    growlBuffer[i] = (wave * 26000).toInt().coerceIn(-32767, 32767).toShort()
                }
                val snapBark = synthesizeBarkPulse(
                    sampleRate = sampleRate,
                    fundamentalHz = 240.0,
                    durationSec = 0.32,
                    pitchDroopHz = 45.0,
                    harmonics = doubleArrayOf(1.0, 0.8, 0.4)
                )
                concatenatePulses(sampleRate, listOf(growlBuffer, snapBark), pauseSec = 0.05)
            }

            // === ALERT EMOTION SYNTHESIZED BARKS ===
            "alert_sentinel" -> {
                // Sentinel attention ping: sharp 470Hz single impulse
                synthesizeBarkPulse(
                    sampleRate = sampleRate,
                    fundamentalHz = 470.0,
                    durationSec = 0.28,
                    pitchDroopHz = 35.0,
                    harmonics = doubleArrayOf(1.0, 0.5, 0.2),
                    breathNoiseRatio = 0.15
                )
            }
            "alert_doorbell" -> {
                // Arrival alert double bark: 510Hz and 530Hz
                val p1 = synthesizeBarkPulse(sampleRate, 510.0, 0.24, pitchDroopHz = 30.0)
                val p2 = synthesizeBarkPulse(sampleRate, 530.0, 0.28, pitchDroopHz = 40.0)
                concatenatePulses(sampleRate, listOf(p1, p2), pauseSec = 0.09)
            }

            // === CURIOUS EMOTION SYNTHESIZED BARKS ===
            "curious_headtilt" -> {
                // Inquisitive head tilt upward sweep: 360Hz up to 580Hz
                val totalDuration = 0.65
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val prog = i.toDouble() / totalSamples
                    val freq = 360.0 + 220.0 * prog.pow(1.3)
                    val env = sin(prog * PI)
                    val wave = (sin(2 * PI * freq * t) + 0.3 * sin(4 * PI * freq * t)) * env
                    buffer[i] = (wave * 27000).toInt().coerceIn(-32767, 32767).toShort()
                }
                buffer
            }

            // === CALM EMOTION SYNTHESIZED BARKS ===
            "calm_settle" -> {
                // Deep soothing 180Hz soft woof with gentle envelope
                synthesizeBarkPulse(
                    sampleRate = sampleRate,
                    fundamentalHz = 180.0,
                    durationSec = 0.55,
                    pitchDroopHz = 20.0,
                    harmonics = doubleArrayOf(1.0, 0.4, 0.15),
                    breathNoiseRatio = 0.12
                )
            }

            // === LEGACY UTILITY AUDIO ===
            "good_dog" -> {
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
                        val env = sin(progress * PI)
                        val wave = (sin(2 * PI * freq * t) + 0.3 * sin(4 * PI * freq * t)) * env
                        buffer[globalIdx] = (wave * 26000).toInt().coerceIn(-32767, 32767).toShort()
                    }
                }
                buffer
            }
            "recall_whistle" -> {
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
                val totalDuration = 0.9
                val totalSamples = (sampleRate * totalDuration).toInt()
                val buffer = ShortArray(totalSamples)
                val mid = totalSamples / 2
                for (i in 0 until mid) {
                    val t = i.toDouble() / sampleRate
                    val env = sin((i.toDouble() / mid) * PI)
                    val wave = sin(2 * PI * 440.0 * t) * env
                    buffer[i] = (wave * 28000).toInt().coerceIn(-32767, 32767).toShort()
                }
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
            "pack_howl" -> {
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
                // Default fallback: 520Hz gentle canine bark pulse
                synthesizeBarkPulse(sampleRate, 520.0, 0.35)
            }
        }
    }
}
