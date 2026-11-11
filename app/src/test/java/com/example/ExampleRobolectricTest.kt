package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
}

