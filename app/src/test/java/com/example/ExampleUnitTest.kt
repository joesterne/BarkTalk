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
    fun testSoundboardItems_emotionMappings() {
        val items = CanineSoundboardPlayer.SOUNDBOARD_ITEMS
        val happyItems = items.filter { it.emotion == "Happy" }
        val playfulItems = items.filter { it.emotion == "Playful" }
        val warningItems = items.filter { it.emotion == "Warning" }

        assertTrue("Happy emotion barks must exist", happyItems.isNotEmpty())
        assertTrue("Playful emotion barks must exist", playfulItems.isNotEmpty())
        assertTrue("Warning emotion barks must exist", warningItems.isNotEmpty())

        val guardBark = items.find { it.id == "warning_guard" }
        assertNotNull(guardBark)
        assertEquals("Warning", guardBark?.emotion)
        assertEquals(220, guardBark?.dominantPitchHz)

        val playBark = items.find { it.id == "playful_bow" }
        assertNotNull(playBark)
        assertEquals("Playful", playBark?.emotion)

        val happyBark = items.find { it.id == "happy_greeting" }
        assertNotNull(happyBark)
        assertEquals("Happy", happyBark?.emotion)
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

    @Test
    fun testTranslationLogEntity_requiredFields() {
        val now = System.currentTimeMillis()
        val translation = com.example.data.TranslationLog(
            petId = 1L,
            petName = "Barnaby",
            petBreed = "Golden Retriever",
            timestamp = now,
            vocalizationType = "Staccato Alert Bark",
            dominantPitchHz = 520,
            intensityDb = 78,
            cadenceDescription = "2 sharp bursts",
            englishTranslation = "Delivery driver at front door!",
            dogThought = "Package arrival detected. Alert sequence activated.",
            detectedEmotion = "Alert & Protective",
            urgencyLevel = "HIGH",
            confidencePercent = 95,
            recommendedAction = "Check front door",
            canineSubtext = "High pitch alert"
        )

        assertEquals(now, translation.timestamp)
        assertEquals(78, translation.intensityDb)
        assertEquals("Delivery driver at front door!", translation.englishTranslation)
        assertEquals("Barnaby", translation.petName)
        assertEquals("HIGH", translation.urgencyLevel)
        assertEquals(false, translation.isFavorite)
    }

    @Test
    fun testSocialShareSnippet_conciseFormat() {
        val translation = com.example.data.TranslationLog(
            petId = 1L,
            petName = "Luna",
            petBreed = "French Bulldog",
            vocalizationType = "High Yip",
            dominantPitchHz = 680,
            intensityDb = 72,
            cadenceDescription = "Short bounce",
            englishTranslation = "Treat cupboard is opening!",
            dogThought = "I demand snacks.",
            detectedEmotion = "Pure Joy",
            urgencyLevel = "LOW",
            confidencePercent = 98,
            recommendedAction = "Give treat",
            canineSubtext = "Friendly solicitation"
        )

        val snippet = com.example.util.SocialShareHelper.buildShareSnippet(
            translation,
            com.example.util.ShareFormat.CONCISE
        )

        assertTrue(snippet.contains("Luna (French Bulldog)"))
        assertTrue(snippet.contains("Treat cupboard is opening!"))
        assertTrue(snippet.contains("I demand snacks."))
        assertTrue(snippet.contains("BarkTalk"))
    }

    @Test
    fun testSocialShareSnippet_detailedAndStoryFormats() {
        val translation = com.example.data.TranslationLog(
            petId = 1L,
            petName = "Zeus",
            petBreed = "German Shepherd",
            vocalizationType = "Low Growl Bark",
            dominantPitchHz = 220,
            intensityDb = 84,
            cadenceDescription = "Deep resonant bursts",
            englishTranslation = "Perimeter intruder alert!",
            dogThought = "Boundary defended.",
            detectedEmotion = "Guard Vigilance",
            urgencyLevel = "HIGH",
            confidencePercent = 94,
            recommendedAction = "Check fence line",
            canineSubtext = "Territorial defense"
        )

        val detailedSnippet = com.example.util.SocialShareHelper.buildShareSnippet(
            translation,
            com.example.util.ShareFormat.DETAILED
        )
        assertTrue(detailedSnippet.contains("220 Hz"))
        assertTrue(detailedSnippet.contains("84 dB"))
        assertTrue(detailedSnippet.contains("Perimeter intruder alert!"))

        val storySnippet = com.example.util.SocialShareHelper.buildShareSnippet(
            translation,
            com.example.util.ShareFormat.STORY_POST
        )
        assertTrue(storySnippet.contains("DISPATCH FROM MY DOG'S BRAIN"))
        assertTrue(storySnippet.contains("Zeus"))
    }

    @Test
    fun testPetProfileDataClass_requiredFields() {
        val pet = com.example.data.PetProfile(
            name = "Barnaby",
            breed = "Golden Retriever",
            age = 4,
            photoUri = "file:///data/user/0/com.aistudio.barktalk/files/pet_photos/pet_123.jpg",
            avatarEmoji = "🦮",
            personality = "Gentle Goofball"
        )

        assertEquals("Barnaby", pet.name)
        assertEquals("Golden Retriever", pet.breed)
        assertEquals(4, pet.age)
        assertEquals(4, pet.ageYears)
        assertEquals("file:///data/user/0/com.aistudio.barktalk/files/pet_photos/pet_123.jpg", pet.photoUri)
        assertEquals("🦮", pet.avatarEmoji)
    }

    @Test
    fun testRecordedBarkSample_preparationAcousticMetrics() {
        val dummyFile = java.io.File.createTempFile("test_sample", ".m4a")
        dummyFile.writeBytes(ByteArray(2048)) // 2KB

        val highClaritySample = com.example.audio.RecordedBarkSample(
            audioFile = dummyFile,
            durationSeconds = 2.4f,
            peakAmplitude = 22000,
            peakDb = 82,
            averageDb = 55, // SNR = 27 dB
            detectedBursts = 3,
            acousticAnalysis = processor.classifyAcoustics(520, 82, 4)
        )

        assertEquals(27, highClaritySample.signalToNoiseRatioDb)
        assertEquals("Crystal Clear", highClaritySample.clarityRating)
        assertEquals(2L, highClaritySample.fileSizeKb)

        val noisySample = com.example.audio.RecordedBarkSample(
            audioFile = dummyFile,
            durationSeconds = 1.0f,
            peakAmplitude = 5000,
            peakDb = 68,
            averageDb = 62, // SNR = 6 dB
            detectedBursts = 1,
            acousticAnalysis = processor.classifyAcoustics(400, 68, 2)
        )

        assertEquals(6, noisySample.signalToNoiseRatioDb)
        assertEquals("High Ambient Noise", noisySample.clarityRating)

        dummyFile.delete()
    }

    @Test
    fun testPreRecordedDogSounds_pcmGenerationAndCache() {
        val player = CanineSoundboardPlayer()
        val items = CanineSoundboardPlayer.SOUNDBOARD_ITEMS
        assertTrue(items.size >= 15)

        val firstItem = items.first()
        val pcmSamples = player.getPreRecordedPcmSamples(firstItem.id)
        assertTrue("Pre-recorded PCM samples must be non-empty", pcmSamples.isNotEmpty())

        // Verify cached reference is returned on subsequent calls
        val cachedSamples = player.getPreRecordedPcmSamples(firstItem.id)
        assertTrue(pcmSamples === cachedSamples)
    }

    @Test
    fun testRecordedBarkEntity_timestampAndAiTranslation() {
        val ts = 1712000000000L
        val recordedBark = com.example.data.RecordedBark(
            timestamp = ts,
            translation = "Someone is knocking on the front door!",
            audioFilePath = "/data/user/0/com.aistudio.barktalk/cache/bark_1.m4a",
            durationSeconds = 2.2f,
            peakDb = 81,
            dominantPitchHz = 490,
            vocalizationType = "Rapid Alarm Bark",
            detectedEmotion = "Alert & Protective",
            petName = "Barnaby",
            petBreed = "Golden Retriever"
        )

        assertEquals(ts, recordedBark.timestamp)
        assertEquals("Someone is knocking on the front door!", recordedBark.translation)
        assertEquals("Someone is knocking on the front door!", recordedBark.aiTranslation)
        assertEquals("Someone is knocking on the front door!", recordedBark.englishTranslation)
        assertEquals(81, recordedBark.peakDb)
        assertEquals("Barnaby", recordedBark.petName)
    }

    @Test
    fun testPdfExporter_generatesValidPdfFromHistoryLogs() {
        val logs = listOf(
            com.example.data.TranslationLog(
                id = 1L,
                petId = 1L,
                petName = "Barnaby",
                petBreed = "Golden Retriever",
                timestamp = 1715000000000L,
                vocalizationType = "Rapid Alarm Bark",
                dominantPitchHz = 510,
                intensityDb = 82,
                cadenceDescription = "3 staccato bursts",
                englishTranslation = "Package delivery at the porch!",
                dogThought = "Must alert the family immediately.",
                detectedEmotion = "Alert & Vigilant",
                urgencyLevel = "HIGH",
                confidencePercent = 96,
                recommendedAction = "Check front door",
                canineSubtext = "Territorial alert"
            )
        )

        val outputStream = java.io.ByteArrayOutputStream()
        com.example.util.BarkHistoryPdfExporter.writePdfToStream(
            logs = logs,
            outputStream = outputStream,
            reportTitle = "Barnaby Weekly Bark Report"
        )

        val pdfBytes = outputStream.toByteArray()
        assertTrue("Exported PDF must be non-empty", pdfBytes.size > 100)
        val header = String(pdfBytes.copyOfRange(0, 5), Charsets.US_ASCII)
        assertEquals("%PDF-", header)
    }

    @Test
    fun testWeeklyBarkFrequencyCalculator_aggregatesLast7Days() {
        val now = 1731420000000L // Reference timestamp
        val oneDayMs = 24L * 60 * 60 * 1000L

        val logs = listOf(
            com.example.data.TranslationLog(
                id = 1L,
                petId = 1L,
                petName = "Barnaby",
                petBreed = "Golden Retriever",
                timestamp = now - 3600_000L, // Today
                vocalizationType = "Rapid Alarm Bark",
                dominantPitchHz = 510,
                intensityDb = 82,
                cadenceDescription = "Rapid",
                englishTranslation = "Doorbell alert!",
                dogThought = "Visitor arrived",
                detectedEmotion = "Alert",
                urgencyLevel = "HIGH",
                confidencePercent = 95,
                recommendedAction = "Check door",
                canineSubtext = "Alert"
            ),
            com.example.data.TranslationLog(
                id = 2L,
                petId = 1L,
                petName = "Barnaby",
                petBreed = "Golden Retriever",
                timestamp = now - 7200_000L, // Today
                vocalizationType = "Playful Yip",
                dominantPitchHz = 600,
                intensityDb = 68,
                cadenceDescription = "Playful",
                englishTranslation = "Throw the ball!",
                dogThought = "Fetch time",
                detectedEmotion = "Playful",
                urgencyLevel = "LOW",
                confidencePercent = 97,
                recommendedAction = "Play fetch",
                canineSubtext = "Play"
            ),
            com.example.data.TranslationLog(
                id = 3L,
                petId = 1L,
                petName = "Barnaby",
                petBreed = "Golden Retriever",
                timestamp = now - (2 * oneDayMs), // 2 days ago
                vocalizationType = "Mournful Howl",
                dominantPitchHz = 360,
                intensityDb = 75,
                cadenceDescription = "Sustained",
                englishTranslation = "Siren song!",
                dogThought = "Pack call",
                detectedEmotion = "Curious",
                urgencyLevel = "MEDIUM",
                confidencePercent = 92,
                recommendedAction = "Reassure",
                canineSubtext = "Howl"
            ),
            com.example.data.TranslationLog(
                id = 4L,
                petId = 1L,
                petName = "Barnaby",
                petBreed = "Golden Retriever",
                timestamp = now - (10 * oneDayMs), // 10 days ago (outside 7-day window)
                vocalizationType = "Old Bark",
                dominantPitchHz = 400,
                intensityDb = 70,
                cadenceDescription = "Single",
                englishTranslation = "Old bark",
                dogThought = "Old thought",
                detectedEmotion = "Calm",
                urgencyLevel = "LOW",
                confidencePercent = 90,
                recommendedAction = "None",
                canineSubtext = "Old"
            )
        )

        val buckets = com.example.ui.components.WeeklyBarkFrequencyCalculator.computeLast7DaysFromLogs(
            logs = logs,
            referenceTimeMillis = now
        )

        assertEquals(7, buckets.size)
        assertEquals(3, buckets.sumOf { it.totalCount })

        val todayBucket = buckets.last()
        assertTrue(todayBucket.isToday)
        assertEquals(2, todayBucket.totalCount)
        assertEquals(1, todayBucket.highUrgencyCount)
        assertEquals(1, todayBucket.standardCount)
        assertEquals(75, todayBucket.averageDb)

        val twoDaysAgoBucket = buckets[4]
        assertEquals(1, twoDaysAgoBucket.totalCount)
        assertEquals("Mournful Howl", twoDaysAgoBucket.dominantVocalization)
    }
}

