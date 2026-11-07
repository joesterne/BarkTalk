package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.TranslationLog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

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
}
