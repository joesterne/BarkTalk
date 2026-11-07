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

@Database(entities = [PetProfile::class, TranslationLog::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petProfileDao(): PetProfileDao
    abstract fun translationDao(): TranslationDao
    fun translationLogDao(): TranslationDao = translationDao()

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
                    }
                }
            }
        }
    }
}
