package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ApplicationProvider
import com.example.audio.CanineSoundboardPlayer
import com.example.audio.SoundboardItem
import com.example.data.AppDatabase
import com.example.data.TranslationLog
import com.example.ui.components.DogSoundboardGridCard
import com.example.ui.theme.BarkTalkTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BarkTalk", appName)
  }

  @Test
  fun `insert and retrieve translation log from Room database`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val dao = database.translationDao()

    val sampleLog = TranslationLog(
      petId = 999L,
      petName = "Rover",
      petBreed = "Beagle",
      timestamp = 1700000000000L,
      vocalizationType = "Howl",
      dominantPitchHz = 380,
      intensityDb = 82,
      cadenceDescription = "Sustained melodic howl",
      englishTranslation = "I hear sirens far away! Singing along!",
      dogThought = "The city is singing, I must answer.",
      detectedEmotion = "Solidarity & Pack Call",
      urgencyLevel = "LOW",
      confidencePercent = 96,
      recommendedAction = "Comfort with gentle petting",
      canineSubtext = "Pack synchronization behavior"
    )

    val insertedId = dao.insertLog(sampleLog)
    assertTrue(insertedId > 0)

    val retrievedLog = dao.getLogById(insertedId)
    assertNotNull(retrievedLog)
    assertEquals("Rover", retrievedLog?.petName)
    assertEquals(82, retrievedLog?.intensityDb)
    assertEquals("I hear sirens far away! Singing along!", retrievedLog?.englishTranslation)
    assertEquals(1700000000000L, retrievedLog?.timestamp)
  }

  @Test
  fun `insert and retrieve pet profile with photo from Room database`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val petDao = database.petProfileDao()

    val pet = com.example.data.PetProfile(
      name = "Copper",
      breed = "Bloodhound",
      age = 2,
      photoUri = "file:///data/user/0/com.aistudio.barktalk/files/pet_photos/copper.jpg",
      avatarEmoji = "🐕",
      personality = "Loyal Guardian"
    )

    val insertedId = petDao.insertProfile(pet)
    assertTrue(insertedId > 0)

    val retrievedPet = petDao.getProfileById(insertedId)
    assertNotNull(retrievedPet)
    assertEquals("Copper", retrievedPet?.name)
    assertEquals("Bloodhound", retrievedPet?.breed)
    assertEquals(2, retrievedPet?.age)
    assertEquals("file:///data/user/0/com.aistudio.barktalk/files/pet_photos/copper.jpg", retrievedPet?.photoUri)
  }

  @Test
  fun `grid soundboard UI plays pre-recorded dog sound when tapped`() {
    var tappedSound: SoundboardItem? = null

    composeTestRule.setContent {
      BarkTalkTheme {
        DogSoundboardGridCard(
          items = CanineSoundboardPlayer.SOUNDBOARD_ITEMS,
          currentlyPlayingId = null,
          lastPlayedSound = null,
          onPlaySound = { item -> tappedSound = item },
          onStopSound = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("main_soundboard_grid").assertExists()
    composeTestRule.onNodeWithTag("soundboard_grid_item_happy_greeting").assertExists().performClick()

    assertNotNull(tappedSound)
    assertEquals("happy_greeting", tappedSound?.id)
    assertEquals("Happy Greeting Bark", tappedSound?.title)
  }

  @Test
  fun `insert and retrieve recorded dog bark with timestamp and AI translation from Room database`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val recordedBarkDao = database.recordedBarkDao()

    val bark = com.example.data.RecordedBark(
      timestamp = 1715000000000L,
      translation = "Throw the frisbee! I'm ready to sprint!",
      audioFilePath = "file:///data/user/0/com.aistudio.barktalk/cache/bark_frisbee.m4a",
      durationSeconds = 1.9f,
      peakDb = 79,
      dominantPitchHz = 590,
      vocalizationType = "Playful Yip",
      detectedEmotion = "Playful Excitement",
      petName = "Barnaby",
      petBreed = "Golden Retriever"
    )

    val id = recordedBarkDao.insertRecordedBark(bark)
    assertTrue(id > 0)

    val retrieved = recordedBarkDao.getRecordedBarkById(id)
    assertNotNull(retrieved)
    assertEquals(1715000000000L, retrieved?.timestamp)
    assertEquals("Throw the frisbee! I'm ready to sprint!", retrieved?.translation)
    assertEquals("Throw the frisbee! I'm ready to sprint!", retrieved?.aiTranslation)
    assertEquals(79, retrieved?.peakDb)
  }

  @Test
  fun `past recordings screen displays stored recordings with timestamp and translation`() {
    val sampleRecordings = listOf(
      com.example.data.RecordedBark(
        id = 101L,
        timestamp = 1715000000000L,
        translation = "Mail carrier is approaching the porch!",
        peakDb = 82,
        dominantPitchHz = 510,
        vocalizationType = "Rapid Alarm Bark",
        detectedEmotion = "Alert",
        petName = "Barnaby",
        petBreed = "Golden Retriever"
      ),
      com.example.data.RecordedBark(
        id = 102L,
        timestamp = 1715003600000L,
        translation = "Can we go for our evening walk now?",
        peakDb = 66,
        dominantPitchHz = 620,
        vocalizationType = "Playful Whine",
        detectedEmotion = "Excited",
        petName = "Luna",
        petBreed = "French Bulldog"
      )
    )

    composeTestRule.setContent {
      BarkTalkTheme {
        com.example.ui.screens.PastRecordingsListContent(
          recordings = sampleRecordings
        )
      }
    }

    composeTestRule.onNodeWithTag("past_recordings_screen").assertExists()
    composeTestRule.onNodeWithTag("past_recordings_list").assertExists()
    composeTestRule.onNodeWithTag("past_recording_card_101").assertExists()
    composeTestRule.onNodeWithTag("recording_timestamp_101").assertExists()
    composeTestRule.onNodeWithTag("recording_translation_101").assertExists()

    composeTestRule.onNodeWithTag("past_recordings_list")
      .performScrollToIndex(1)

    composeTestRule.onNodeWithTag("past_recording_card_102").assertExists()
    composeTestRule.onNodeWithTag("recording_timestamp_102").assertExists()
    composeTestRule.onNodeWithTag("recording_translation_102").assertExists()
  }

  @Test
  fun `export translation history logs and recorded barks to shareable PDF file`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sampleLogs = listOf(
      TranslationLog(
        id = 201L,
        petId = 1L,
        petName = "Barnaby",
        petBreed = "Golden Retriever",
        timestamp = 1715000000000L,
        vocalizationType = "Rapid Alarm Bark",
        dominantPitchHz = 510,
        intensityDb = 82,
        cadenceDescription = "3 staccato bursts",
        englishTranslation = "Mail carrier is at the front gate!",
        dogThought = "Perimeter breach detected.",
        detectedEmotion = "Alert",
        urgencyLevel = "HIGH",
        confidencePercent = 95,
        recommendedAction = "Look out the window",
        canineSubtext = "Guard instinct"
      )
    )

    val pdfFile = com.example.util.BarkHistoryPdfExporter.exportTranslationLogsToPdfFile(
      context = context,
      logs = sampleLogs,
      reportTitle = "Barnaby Bark Translation Log"
    )

    assertTrue("PDF file should exist", pdfFile.exists())
    assertTrue("PDF file should have .pdf extension", pdfFile.name.endsWith(".pdf"))
    assertTrue("PDF file should be non-empty", pdfFile.length() > 100)

    val header = pdfFile.inputStream().use { stream ->
      val buf = ByteArray(5)
      stream.read(buf)
      String(buf, Charsets.US_ASCII)
    }
    assertEquals("%PDF-", header)
  }

  @Test
  fun `past recordings screen opens PDF export dialog when PDF button is clicked`() {
    val sampleRecordings = listOf(
      com.example.data.RecordedBark(
        id = 301L,
        timestamp = 1715000000000L,
        translation = "Let's play fetch in the backyard!",
        peakDb = 75,
        dominantPitchHz = 580,
        vocalizationType = "Playful Bark",
        detectedEmotion = "Playful",
        petName = "Barnaby",
        petBreed = "Golden Retriever"
      )
    )

    composeTestRule.setContent {
      BarkTalkTheme {
        com.example.ui.screens.PastRecordingsListContent(
          recordings = sampleRecordings
        )
      }
    }

    composeTestRule.onNodeWithTag("export_past_recordings_pdf_button").assertExists().performClick()
    composeTestRule.onNodeWithTag("pdf_export_dialog").assertExists()
    composeTestRule.onNodeWithTag("confirm_share_pdf_button").assertExists()
    composeTestRule.onNodeWithTag("save_pdf_to_device_button").assertExists()
  }
}

