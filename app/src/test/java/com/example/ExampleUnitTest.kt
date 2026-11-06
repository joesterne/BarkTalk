package com.example

import com.example.audio.AudioProcessor
import com.example.audio.CanineSoundboardPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private val processor = AudioProcessor()

    @Test
    fun testAcousticClassification_highWhine() {
        val analysis = processor.classifyAcoustics(pitchHz = 750, volumeDb = 60, durationChunks = 5)
        assertEquals("High Whine / Whimper", analysis.vocalizationType)
        assertEquals(750, analysis.pitchHz)
        assertTrue(analysis.confidence >= 90)
    }

    @Test
    fun testAcousticClassification_deepGrowl() {
        val analysis = processor.classifyAcoustics(pitchHz = 190, volumeDb = 75, durationChunks = 4)
        assertEquals("Low Resonant Growl", analysis.vocalizationType)
    }

    @Test
    fun testAcousticClassification_howl() {
        val analysis = processor.classifyAcoustics(pitchHz = 340, volumeDb = 85, durationChunks = 12)
        assertEquals("Mournful Canine Howl", analysis.vocalizationType)
    }

    @Test
    fun testAcousticClassification_alertBark() {
        val analysis = processor.classifyAcoustics(pitchHz = 500, volumeDb = 80, durationChunks = 2)
        assertEquals("Staccato Alert Bark", analysis.vocalizationType)
    }

    @Test
    fun testSoundboardItems_notEmpty() {
        val items = CanineSoundboardPlayer.SOUNDBOARD_ITEMS
        assertTrue(items.isNotEmpty())
        items.forEach { item ->
            assertNotNull(item.id)
            assertNotNull(item.title)
            assertNotNull(item.canineMeaning)
        }
    }

    @Test
    fun testWidgetMoodCalculation_alert() {
        val log = com.example.data.TranslationLog(
            petId = 1,
            petName = "Barnaby",
            petBreed = "Golden Retriever",
            vocalizationType = "Alarm Bark",
            dominantPitchHz = 500,
            intensityDb = 80,
            cadenceDescription = "Rapid bursts",
            englishTranslation = "Intruder at fence!",
            dogThought = "Guard mode",
            detectedEmotion = "Alert & Vigilant",
            urgencyLevel = "HIGH",
            confidencePercent = 95,
            recommendedAction = "Check yard",
            canineSubtext = "High pitch alert"
        )
        val mood = com.example.widget.BarkTalkWidgetProvider.calculateMoodIndicator(log)
        assertTrue(mood.contains("Alert"))
    }

    @Test
    fun testWidgetMoodCalculation_playful() {
        val log = com.example.data.TranslationLog(
            petId = 1,
            petName = "Barnaby",
            petBreed = "Golden Retriever",
            vocalizationType = "Play Yip",
            dominantPitchHz = 620,
            intensityDb = 65,
            cadenceDescription = "Playful",
            englishTranslation = "Throw the tennis ball!",
            dogThought = "Fetch is life",
            detectedEmotion = "Pure Joy & Play",
            urgencyLevel = "LOW",
            confidencePercent = 98,
            recommendedAction = "Play fetch",
            canineSubtext = "Playful bounce"
        )
        val mood = com.example.widget.BarkTalkWidgetProvider.calculateMoodIndicator(log)
        assertTrue(mood.contains("Playful"))
    }

    @Test
    fun testWidgetMoodCalculation_nullLog() {
        val mood = com.example.widget.BarkTalkWidgetProvider.calculateMoodIndicator(null)
        assertEquals("🐾 Calm & Ready", mood)
    }

    @Test
    fun testMediaRecorderClassification_rapidAlarmBark() {
        val analysis = com.example.audio.BarkMediaRecorder.classifySample(
            durationSec = 1.8f,
            peakDb = 82,
            avgDb = 68,
            burstCount = 4
        )
        assertEquals("Rapid Alarm Bark", analysis.vocalizationType)
        assertTrue(analysis.pitchHz in 460..540)
        assertTrue(analysis.cadenceDescription.contains("4 rapid bark bursts"))
    }

    @Test
    fun testMediaRecorderClassification_howl() {
        val analysis = com.example.audio.BarkMediaRecorder.classifySample(
            durationSec = 3.0f,
            peakDb = 75,
            avgDb = 64,
            burstCount = 1
        )
        assertEquals("Mournful Canine Howl", analysis.vocalizationType)
    }

    @Test
    fun testMediaRecorderClassification_highWhine() {
        val analysis = com.example.audio.BarkMediaRecorder.classifySample(
            durationSec = 1.5f,
            peakDb = 62,
            avgDb = 52,
            burstCount = 1
        )
        assertEquals("High Whine & Whimper", analysis.vocalizationType)
        assertTrue(analysis.pitchHz in 650..780)
    }
}

