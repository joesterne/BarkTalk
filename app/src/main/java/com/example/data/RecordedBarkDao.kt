package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for storing, querying, and managing recorded dog barks
 * along with their recording timestamp and AI-generated translation.
 */
@Dao
interface RecordedBarkDao {

    /**
     * Retrieves all recorded dog barks ordered by timestamp descending (newest first).
     */
    @Query("SELECT * FROM recorded_barks ORDER BY timestamp DESC")
    fun getAllRecordedBarks(): Flow<List<RecordedBark>>

    /**
     * Retrieves recorded dog barks for a specific pet profile ordered by timestamp descending.
     */
    @Query("SELECT * FROM recorded_barks WHERE petId = :petId ORDER BY timestamp DESC")
    fun getRecordedBarksForPet(petId: Long): Flow<List<RecordedBark>>

    /**
     * Retrieves bookmarked/favorite recorded dog barks.
     */
    @Query("SELECT * FROM recorded_barks WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteRecordedBarks(): Flow<List<RecordedBark>>

    /**
     * Retrieves the most recently recorded dog bark.
     */
    @Query("SELECT * FROM recorded_barks ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecordedBark(): Flow<RecordedBark?>

    /**
     * Retrieves a single recorded dog bark by its primary key ID.
     */
    @Query("SELECT * FROM recorded_barks WHERE id = :id LIMIT 1")
    suspend fun getRecordedBarkById(id: Long): RecordedBark?

    /**
     * Searches recorded dog barks by AI-generated translation text or vocalization type.
     */
    @Query("SELECT * FROM recorded_barks WHERE translation LIKE '%' || :query || '%' OR vocalizationType LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecordedBarks(query: String): Flow<List<RecordedBark>>

    /**
     * Returns the total count of stored recorded dog barks.
     */
    @Query("SELECT COUNT(*) FROM recorded_barks")
    fun getRecordedBarkCount(): Flow<Int>

    /**
     * Inserts a new recorded dog bark into the database and returns its row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordedBark(bark: RecordedBark): Long

    /**
     * Inserts multiple recorded dog barks in batch.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordedBarks(barks: List<RecordedBark>): List<Long>

    /**
     * Updates an existing recorded dog bark record.
     */
    @Update
    suspend fun updateRecordedBark(bark: RecordedBark)

    /**
     * Updates the AI-generated translation and emotion metadata for a recorded dog bark.
     */
    @Query("UPDATE recorded_barks SET translation = :translation, detectedEmotion = :emotion, dogThought = :dogThought WHERE id = :id")
    suspend fun updateAiTranslation(
        id: Long,
        translation: String,
        emotion: String = "Alert",
        dogThought: String = ""
    )

    /**
     * Toggles or sets the favorite status of a recorded dog bark.
     */
    @Query("UPDATE recorded_barks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    /**
     * Deletes a specific recorded dog bark entity.
     */
    @Delete
    suspend fun deleteRecordedBark(bark: RecordedBark)

    /**
     * Deletes a recorded dog bark by its primary key ID.
     */
    @Query("DELETE FROM recorded_barks WHERE id = :id")
    suspend fun deleteRecordedBarkById(id: Long)

    /**
     * Clears all recorded dog barks from the table.
     */
    @Query("DELETE FROM recorded_barks")
    suspend fun clearAllRecordedBarks()
}

typealias BarkDao = RecordedBarkDao
