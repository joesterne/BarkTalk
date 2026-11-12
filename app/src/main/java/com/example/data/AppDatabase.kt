package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface PetProfileDao {
    @Query("SELECT * FROM pet_profiles ORDER BY isSelected DESC, name ASC")
    fun getAllProfiles(): Flow<List<PetProfile>>

    @Query("SELECT * FROM pet_profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedProfile(): Flow<PetProfile?>

    @Query("SELECT * FROM pet_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): PetProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PetProfile): Long

    @Update
    suspend fun updateProfile(profile: PetProfile)

    @Delete
    suspend fun deleteProfile(profile: PetProfile)

    @Query("UPDATE pet_profiles SET isSelected = 0")
    suspend fun clearSelection()

    @Query("UPDATE pet_profiles SET isSelected = 1 WHERE id = :id")
    suspend fun selectProfile(id: Long)
}

@Database(
    entities = [PetProfile::class, TranslationLog::class, RecordedBark::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petProfileDao(): PetProfileDao
    abstract fun translationDao(): TranslationDao
    abstract fun recordedBarkDao(): RecordedBarkDao
    fun translationLogDao(): TranslationDao = translationDao()
    fun barkDao(): RecordedBarkDao = recordedBarkDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barktalk_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        val petDao = database.petProfileDao()
                        val logDao = database.translationLogDao()

                        // Insert starter default pet profiles
                        val petId1 = petDao.insertProfile(
                            PetProfile(
                                name = "Barnaby",
                                breed = "Golden Retriever",
                                ageYears = 3,
                                ageMonths = 4,
                                avatarEmoji = "🦮",
                                personality = "Gentle Goofball",
                                pitchProfile = "Standard Bark",
                                favoriteTreat = "Cheddar Cheese cubes",
                                isSelected = true
                            )
                        )
                        petDao.insertProfile(
                            PetProfile(
                                name = "Luna",
                                breed = "French Bulldog",
                                ageYears = 2,
                                ageMonths = 1,
                                avatarEmoji = "🐕",
                                personality = "Dramatic Diva",
                                pitchProfile = "High Yip",
                                favoriteTreat = "Freeze-dried Liver",
                                isSelected = false
                            )
                        )
                        petDao.insertProfile(
                            PetProfile(
                                name = "Zeus",
                                breed = "German Shepherd",
                                ageYears = 4,
                                ageMonths = 8,
                                avatarEmoji = "🐺",
                                personality = "Loyal Guardian",
                                pitchProfile = "Deep Woof",
                                favoriteTreat = "Beef marrow bone",
                                isSelected = false
                            )
                        )

                        // Insert starter sample translation logs
                        logDao.insertLog(
                            TranslationLog(
                                petId = petId1,
                                petName = "Barnaby",
                                petBreed = "Golden Retriever",
                                timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
                                vocalizationType = "Rapid Alarm Bark",
                                dominantPitchHz = 480,
                                intensityDb = 78,
                                cadenceDescription = "3 sharp staccato bursts, 0.4s interval",
                                englishTranslation = "Squirrel! Alert! Fluffy-tailed intruder perched upon the cedar fence!",
                                dogThought = "Must notify human instantly. I have barked at it 400 times, today will surely be the day it surrenders.",
                                detectedEmotion = "Alert & Vigilant",
                                urgencyLevel = "HIGH",
                                confidencePercent = 94,
                                recommendedAction = "Acknowledge the alert with calm reassurance or close blinds.",
                                canineSubtext = "Moderate-to-high pitch with rapid repetition signifies territorial excitement and social rallying.",
                                isFavorite = true
                            )
                        )
                        logDao.insertLog(
                            TranslationLog(
                                petId = petId1,
                                petName = "Barnaby",
                                petBreed = "Golden Retriever",
                                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                                vocalizationType = "Playful Yip & Whine",
                                dominantPitchHz = 610,
                                intensityDb = 62,
                                cadenceDescription = "Melodic rising pitch with tail wagging",
                                englishTranslation = "Did you just touch the treat cupboard door? You definitely did!",
                                dogThought = "I am sitting very politely. Look at my puppy eyes. Resistance is futile.",
                                detectedEmotion = "Anticipation & Joy",
                                urgencyLevel = "LOW",
                                confidencePercent = 98,
                                recommendedAction = "Reward good behavior with a healthy treat or play session.",
                                canineSubtext = "High harmonic whine coupled with harmonic pitch glide indicates non-distress positive solicitation.",
                                isFavorite = false
                            )
                        )

                        val recordedBarkDao = database.recordedBarkDao()
                        recordedBarkDao.insertRecordedBark(
                            RecordedBark(
                                petId = petId1,
                                petName = "Barnaby",
                                petBreed = "Golden Retriever",
                                timestamp = System.currentTimeMillis() - 1000L * 60 * 18,
                                translation = "Squirrel! Alert! Fluffy-tailed intruder perched upon the cedar fence!",
                                durationSeconds = 2.1f,
                                peakDb = 78,
                                dominantPitchHz = 480,
                                vocalizationType = "Rapid Alarm Bark",
                                cadenceDescription = "3 sharp staccato bursts, 0.4s interval",
                                detectedEmotion = "Alert & Vigilant",
                                dogThought = "Must notify human instantly.",
                                urgencyLevel = "HIGH",
                                confidencePercent = 94,
                                isFavorite = true
                            )
                        )
                        recordedBarkDao.insertRecordedBark(
                            RecordedBark(
                                petId = petId1,
                                petName = "Barnaby",
                                petBreed = "Golden Retriever",
                                timestamp = System.currentTimeMillis() - 1000L * 60 * 120,
                                translation = "Did you just touch the treat cupboard door? You definitely did!",
                                durationSeconds = 1.6f,
                                peakDb = 62,
                                dominantPitchHz = 610,
                                vocalizationType = "Playful Yip & Whine",
                                cadenceDescription = "Melodic rising pitch with tail wagging",
                                detectedEmotion = "Anticipation & Joy",
                                dogThought = "I am sitting very politely.",
                                urgencyLevel = "LOW",
                                confidencePercent = 98,
                                isFavorite = false
                            )
                        )

                        // Seed historical barks across the last 7 days for rich 7-day bar chart analytics
                        val dayMs = 24L * 60 * 60 * 1000L
                        val multiDaySeeds = listOf(
                            Triple(1, "Mail carrier is at the front gate!", "HIGH"),
                            Triple(2, "Throw the tennis ball! I'm ready to sprint!", "LOW"),
                            Triple(2, "Can we go for our evening walk in the park?", "MEDIUM"),
                            Triple(3, "Strange cat spotted near the garden hedge!", "HIGH"),
                            Triple(5, "My water bowl is getting low, human!", "MEDIUM"),
                            Triple(6, "Welcome home! I missed you all afternoon!", "LOW")
                        )
                        multiDaySeeds.forEachIndexed { idx, (daysAgo, translationText, urgency) ->
                            val seedTs = System.currentTimeMillis() - (daysAgo * dayMs) - (idx * 3600_000L)
                            logDao.insertLog(
                                TranslationLog(
                                    petId = petId1,
                                    petName = "Barnaby",
                                    petBreed = "Golden Retriever",
                                    timestamp = seedTs,
                                    vocalizationType = if (urgency == "HIGH") "Rapid Alarm Bark" else "Playful Greeting Bark",
                                    dominantPitchHz = if (urgency == "HIGH") 510 else 440,
                                    intensityDb = if (urgency == "HIGH") 81 else 68,
                                    cadenceDescription = "Rhythmic canine vocalization",
                                    englishTranslation = translationText,
                                    dogThought = "Communicating daily canine priorities clearly.",
                                    detectedEmotion = if (urgency == "HIGH") "Alert & Vigilant" else "Happy & Social",
                                    urgencyLevel = urgency,
                                    confidencePercent = 95,
                                    recommendedAction = "Respond with calm attention.",
                                    canineSubtext = "Recorded during 7-day acoustic baseline."
                                )
                            )
                            recordedBarkDao.insertRecordedBark(
                                RecordedBark(
                                    petId = petId1,
                                    petName = "Barnaby",
                                    petBreed = "Golden Retriever",
                                    timestamp = seedTs,
                                    translation = translationText,
                                    durationSeconds = 1.9f,
                                    peakDb = if (urgency == "HIGH") 81 else 68,
                                    dominantPitchHz = if (urgency == "HIGH") 510 else 440,
                                    vocalizationType = if (urgency == "HIGH") "Rapid Alarm Bark" else "Playful Greeting Bark",
                                    cadenceDescription = "Rhythmic canine vocalization",
                                    detectedEmotion = if (urgency == "HIGH") "Alert & Vigilant" else "Happy & Social",
                                    dogThought = "Communicating daily canine priorities clearly.",
                                    urgencyLevel = urgency,
                                    confidencePercent = 95
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
