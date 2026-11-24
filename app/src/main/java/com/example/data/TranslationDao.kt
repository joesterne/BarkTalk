package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for dog vocalization translation records.
 * Provides reactive Flow streams and suspend functions to persist, query, filter,
 * and manage past canine translations, including timestamp, original audio intensity,
 * and translated text so users can view their dog's communication history.
 */
@Dao
interface TranslationDao {

    /**
     * Retrieves all communication history logs ordered by timestamp descending (newest first).
     */
    @Query("SELECT * FROM translation_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TranslationLog>>

    /**
     * Alias for getAllLogs returning all past translations.
     */
    @Query("SELECT * FROM translation_logs ORDER BY timestamp DESC")
    fun getAllTranslations(): Flow<List<TranslationLog>>

    /**
     * Retrieves communication history for a specific dog profile.
     */
    @Query("SELECT * FROM translation_logs WHERE petId = :petId ORDER BY timestamp DESC")
    fun getLogsForPet(petId: Long): Flow<List<TranslationLog>>

    /**
     * Retrieves favorite bookmarked translations.
     */
    @Query("SELECT * FROM translation_logs WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteLogs(): Flow<List<TranslationLog>>

    /**
     * Retrieves a single translation by its primary key ID.
     */
    @Query("SELECT * FROM translation_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): TranslationLog?

    /**
     * Retrieves the most recent canine translation.
     */
    @Query("SELECT * FROM translation_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLog(): Flow<TranslationLog?>

    /**
     * Searches translation history by keywords in the translated text, dog thought, or optional dog tag.
     */
    @Query("SELECT * FROM translation_logs WHERE englishTranslation LIKE '%' || :query || '%' OR dogThought LIKE '%' || :query || '%' OR dogTag LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTranslations(query: String): Flow<List<TranslationLog>>

    /**
     * Retrieves translation logs matching a specific user-assigned dog tag.
     */
    @Query("SELECT * FROM translation_logs WHERE dogTag = :dogTag ORDER BY timestamp DESC")
    fun getLogsByDogTag(dogTag: String): Flow<List<TranslationLog>>

    /**
     * Retrieves all translation logs that have an assigned dog tag.
     */
    @Query("SELECT * FROM translation_logs WHERE dogTag IS NOT NULL AND TRIM(dogTag) != '' ORDER BY timestamp DESC")
    fun getTaggedLogs(): Flow<List<TranslationLog>>

    /**
     * Retrieves all distinct non-empty user-assigned dog tags sorted alphabetically.
     */
    @Query("SELECT DISTINCT dogTag FROM translation_logs WHERE dogTag IS NOT NULL AND TRIM(dogTag) != '' ORDER BY dogTag ASC")
    fun getAllDogTags(): Flow<List<String>>

    /**
     * Filters communication history by detected canine emotion (e.g. Happy, Alert, Playful).
     */
    @Query("SELECT * FROM translation_logs WHERE detectedEmotion LIKE '%' || :emotion || '%' ORDER BY timestamp DESC")
    fun getTranslationsByEmotion(emotion: String): Flow<List<TranslationLog>>

    /**
     * Filters history by minimum original audio intensity in decibels (dB).
     */
    @Query("SELECT * FROM translation_logs WHERE intensityDb >= :minDb ORDER BY intensityDb DESC, timestamp DESC")
    fun getTranslationsByMinIntensity(minDb: Int): Flow<List<TranslationLog>>

    /**
     * Returns total count of saved translations.
     */
    @Query("SELECT COUNT(*) FROM translation_logs")
    fun getTranslationCount(): Flow<Int>

    /**
     * Inserts a new translation into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TranslationLog): Long

    /**
     * Inserts multiple translations in batch.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationLog>): List<Long>

    /**
     * Updates an existing translation record.
     */
    @Update
    suspend fun updateLog(log: TranslationLog)

    /**
     * Assigns, updates, or clears (null) the optional user-assigned dog tag for a translation log.
     */
    @Query("UPDATE translation_logs SET dogTag = :dogTag WHERE id = :id")
    suspend fun updateDogTag(id: Long, dogTag: String?)

    /**
     * Deletes a specific translation record.
     */
    @Delete
    suspend fun deleteLog(log: TranslationLog)

    /**
     * Deletes a translation record by ID.
     */
    @Query("DELETE FROM translation_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    /**
     * Updates the favorite status of a translation log.
     */
    @Query("UPDATE translation_logs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    /**
     * Clears all communication history logs.
     */
    @Query("DELETE FROM translation_logs")
    suspend fun clearAllLogs()
}

/**
 * Backward-compatible alias for existing codebase usages.
 */
typealias TranslationLogDao = TranslationDao
