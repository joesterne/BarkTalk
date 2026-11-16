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

  @Test
  fun `weekly bark frequency bar chart displays 7 days of translations and supports day inspection`() {
    val now = 1731420000000L
    val oneDayMs = 24L * 60 * 60 * 1000L

    val logs = listOf(
      TranslationLog(
        id = 401L,
        petId = 1L,
        petName = "Barnaby",
        petBreed = "Golden Retriever",
        timestamp = now - 1800_000L, // Today (dayIndex = 6)
        vocalizationType = "Rapid Alarm Bark",
        dominantPitchHz = 510,
        intensityDb = 82,
        cadenceDescription = "Rapid",
        englishTranslation = "Squirrel on the fence!",
        dogThought = "Intruder alert",
        detectedEmotion = "Alert",
        urgencyLevel = "HIGH",
        confidencePercent = 96,
        recommendedAction = "Check yard",
        canineSubtext = "Territorial"
      ),
      TranslationLog(
        id = 402L,
        petId = 1L,
        petName = "Barnaby",
        petBreed = "Golden Retriever",
        timestamp = now - (3 * oneDayMs), // 3 days ago (dayIndex = 3)
        vocalizationType = "Playful Yip",
        dominantPitchHz = 590,
        intensityDb = 70,
        cadenceDescription = "Bouncy",
        englishTranslation = "Let's play tug-of-war!",
        dogThought = "Playtime",
        detectedEmotion = "Playful",
        urgencyLevel = "LOW",
        confidencePercent = 94,
        recommendedAction = "Play",
        canineSubtext = "Play"
      ),
      TranslationLog(
        id = 403L,
        petId = 1L,
        petName = "Barnaby",
        petBreed = "Golden Retriever",
        timestamp = now - (3 * oneDayMs) - 3600_000L, // Also 3 days ago (dayIndex = 3)
        vocalizationType = "Playful Yip",
        dominantPitchHz = 605,
        intensityDb = 72,
        cadenceDescription = "Bouncy",
        englishTranslation = "More treats please!",
        dogThought = "Hungry",
        detectedEmotion = "Joy",
        urgencyLevel = "LOW",
        confidencePercent = 98,
        recommendedAction = "Give snack",
        canineSubtext = "Solicitation"
      )
    )

    val buckets = com.example.ui.components.WeeklyBarkFrequencyCalculator.computeLast7DaysFromLogs(
      logs = logs,
      referenceTimeMillis = now
    )

    composeTestRule.setContent {
      BarkTalkTheme {
        com.example.ui.components.WeeklyBarkFrequencyChart(
          buckets = buckets
        )
      }
    }

    composeTestRule.onNodeWithTag("weekly_bark_frequency_chart").assertExists()
    composeTestRule.onNodeWithTag("weekly_bark_canvas").assertExists()
    composeTestRule.onNodeWithTag("weekly_chart_total_badge").assertExists()
    composeTestRule.onNodeWithTag("weekly_chart_tooltip").assertExists()

    // Verify all 7 day columns exist on the X-axis and tap dayIndex 3 (which has 2 barks)
    for (dayIdx in 0..6) {
      composeTestRule.onNodeWithTag("weekly_chart_day_$dayIdx").assertExists()
    }
    composeTestRule.onNodeWithTag("weekly_chart_day_3").performClick()
    composeTestRule.onNodeWithTag("weekly_chart_selected_day_count").assertExists()
  }

  @Test
  fun `recording screen dog profile selector associates recording via selection chips and dropdown`() {
    val profiles = listOf(
      com.example.data.PetProfile(
        id = 1L,
        name = "Barnaby",
        breed = "Golden Retriever",
        age = 3,
        avatarEmoji = "🦮"
      ),
      com.example.data.PetProfile(
        id = 2L,
        name = "Luna",
        breed = "French Bulldog",
        age = 2,
        avatarEmoji = "🐕"
      ),
      com.example.data.PetProfile(
        id = 3L,
        name = "Zeus",
        breed = "German Shepherd",
        age = 4,
        avatarEmoji = "🐺"
      )
    )

    var selectedProfile: com.example.data.PetProfile? = profiles[0]

    composeTestRule.setContent {
      val currentState = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(selectedProfile)
      }
      BarkTalkTheme {
        com.example.ui.screens.DogProfileRecordingSelector(
          profiles = profiles,
          selectedPet = currentState.value,
          onSelectPet = { chosen ->
            currentState.value = chosen
            selectedProfile = chosen
          },
          onManageProfiles = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("dog_profile_selector_card").assertExists()
    composeTestRule.onNodeWithTag("dog_profile_chips_row").assertExists()
    composeTestRule.onNodeWithTag("dog_profile_chip_2").assertExists().performClick()

    assertEquals("Luna", selectedProfile?.name)
    assertEquals("French Bulldog", selectedProfile?.breed)

    // Also test selecting via the dropdown menu
    composeTestRule.onNodeWithTag("dog_profile_dropdown_button").assertExists().performClick()
    composeTestRule.onNodeWithTag("dog_profile_dropdown_item_3").assertExists().performClick()

    assertEquals("Zeus", selectedProfile?.name)
    assertEquals("German Shepherd", selectedProfile?.breed)
  }

  @Test
  fun `daily bark goal card displays progress bar and updates goal target`() {
    var currentGoal = 5

    composeTestRule.setContent {
      val goalState = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(currentGoal)
      }
      BarkTalkTheme {
        com.example.ui.components.DailyBarkGoalCard(
          todayAnalyzedCount = 3,
          dailyGoal = goalState.value,
          onUpdateDailyGoal = { newGoal ->
            goalState.value = newGoal
            currentGoal = newGoal
          }
        )
      }
    }

    composeTestRule.onNodeWithTag("daily_bark_goal_card").assertExists()
    composeTestRule.onNodeWithTag("daily_bark_goal_progress_bar").assertExists()
    composeTestRule.onNodeWithTag("daily_bark_goal_progress_text").assertExists()
    composeTestRule.onNodeWithTag("daily_bark_goal_percent_badge").assertExists()

    // Select the 10/day preset chip
    composeTestRule.onNodeWithTag("daily_goal_preset_10").assertExists().performClick()
    assertEquals(10, currentGoal)

    // Increment by 1 using the + button
    composeTestRule.onNodeWithTag("increase_daily_goal_button").assertExists().performClick()
    assertEquals(11, currentGoal)
  }

  @Test
  fun `latest bark quick share card and intent share latest translation`() {
    val latestLog = TranslationLog(
      id = 501L,
      petId = 1L,
      petName = "Barnaby",
      petBreed = "Golden Retriever",
      vocalizationType = "Playful Yip",
      dominantPitchHz = 600,
      intensityDb = 74,
      cadenceDescription = "2 joyful yips",
      englishTranslation = "Take me to the dog park right now!",
      dogThought = "Zoomies incoming.",
      detectedEmotion = "Excited & Playful",
      urgencyLevel = "MEDIUM",
      confidencePercent = 97,
      recommendedAction = "Grab the leash",
      canineSubtext = "Social play invitation"
    )

    val chooserIntent = com.example.util.SocialShareHelper.createShareIntent(latestLog)
    assertEquals(android.content.Intent.ACTION_CHOOSER, chooserIntent.action)
    @Suppress("DEPRECATION")
    val targetIntent = chooserIntent.getParcelableExtra<android.content.Intent>(android.content.Intent.EXTRA_INTENT)
    assertNotNull(targetIntent)
    assertEquals(android.content.Intent.ACTION_SEND, targetIntent?.action)
    assertEquals("text/plain", targetIntent?.type)
    val sharedText = targetIntent?.getStringExtra(android.content.Intent.EXTRA_TEXT) ?: ""
    assertTrue(sharedText.contains("Take me to the dog park right now!"))
    assertTrue(sharedText.contains("Barnaby"))

    composeTestRule.setContent {
      BarkTalkTheme {
        com.example.ui.components.LatestBarkQuickShareCard(
          latestLog = latestLog,
          onOpenFormatDialog = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("latest_bark_quick_share_card").assertExists()
    composeTestRule.onNodeWithTag("latest_bark_translation_preview_text").assertExists()
    composeTestRule.onNodeWithTag("quick_share_latest_bark_button").assertExists().performClick()
  }

  @Test
  fun `favorites screen displays favorite bark translations and marks recent translations as favorites`() {
    val initialLogs = listOf(
      TranslationLog(
        id = 601L,
        petId = 1L,
        petName = "Barnaby",
        petBreed = "Golden Retriever",
        vocalizationType = "Rapid Alarm Bark",
        dominantPitchHz = 510,
        intensityDb = 82,
        cadenceDescription = "Rapid bursts",
        englishTranslation = "Squirrel perched on the garden fence!",
        dogThought = "Must protect the yard.",
        detectedEmotion = "Alert",
        urgencyLevel = "HIGH",
        confidencePercent = 96,
        recommendedAction = "Check backyard",
        canineSubtext = "Territorial alert",
        isFavorite = true
      ),
      TranslationLog(
        id = 602L,
        petId = 2L,
        petName = "Luna",
        petBreed = "French Bulldog",
        vocalizationType = "Playful Yip",
        dominantPitchHz = 640,
        intensityDb = 66,
        cadenceDescription = "Short yip",
        englishTranslation = "Dinner bowl is empty, human!",
        dogThought = "Feed me immediately.",
        detectedEmotion = "Playful",
        urgencyLevel = "LOW",
        confidencePercent = 98,
        recommendedAction = "Serve dinner",
        canineSubtext = "Food solicitation",
        isFavorite = false
      )
    )

    composeTestRule.setContent {
      val logsState = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(initialLogs)
      }
      val favs = logsState.value.filter { it.isFavorite }

      BarkTalkTheme {
        com.example.ui.screens.FavoritesScreenContent(
          favoriteLogs = favs,
          allLogs = logsState.value,
          onToggleFavorite = { target ->
            logsState.value = logsState.value.map {
              if (it.id == target.id) it.copy(isFavorite = !it.isFavorite) else it
            }
          }
        )
      }
    }

    composeTestRule.onNodeWithTag("favorites_screen").assertExists()
    composeTestRule.onNodeWithTag("favorites_count_badge").assertExists()
    composeTestRule.onNodeWithTag("favorites_list").assertExists()
    composeTestRule.onNodeWithTag("favorite_card_601").assertExists()
    composeTestRule.onNodeWithTag("favorite_translation_text_601").assertExists()

    // Mark log 602 as a favorite using the quick-mark chip on the Favorites screen
    composeTestRule.onNodeWithTag("quick_mark_favorites_section").assertExists()
    composeTestRule.onNodeWithTag("mark_as_favorite_chip_602").assertExists().performClick()

    // Scroll to index 1 in the LazyColumn and verify log 602 now appears in the Favorites list
    composeTestRule.onNodeWithTag("favorites_list").performScrollToIndex(1)
    composeTestRule.onNodeWithTag("favorite_card_602").assertExists()
  }
}
