package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing recorded dog barks and their AI-generated translations
 * backed by Room database persistence.
 */
class RecordedBarkRepository(private val recordedBarkDao: RecordedBarkDao) {

    val allRecordedBarks: Flow<List<RecordedBark>> = recordedBarkDao.getAllRecordedBarks()

    val favoriteRecordedBarks: Flow<List<RecordedBark>> = recordedBarkDao.getFavoriteRecordedBarks()

    val latestRecordedBark: Flow<RecordedBark?> = recordedBarkDao.getLatestRecordedBark()

    val recordedBarkCount: Flow<Int> = recordedBarkDao.getRecordedBarkCount()

    fun getRecordedBarksForPet(petId: Long): Flow<List<RecordedBark>> {
        return recordedBarkDao.getRecordedBarksForPet(petId)
    }

    fun searchRecordedBarks(query: String): Flow<List<RecordedBark>> {
        return recordedBarkDao.searchRecordedBarks(query)
    }

    suspend fun getRecordedBarkById(id: Long): RecordedBark? {
        return recordedBarkDao.getRecordedBarkById(id)
    }

    suspend fun insertRecordedBark(bark: RecordedBark): Long {
        return recordedBarkDao.insertRecordedBark(bark)
    }

    suspend fun insertRecordedBarks(barks: List<RecordedBark>): List<Long> {
        return recordedBarkDao.insertRecordedBarks(barks)
    }

    suspend fun updateRecordedBark(bark: RecordedBark) {
        recordedBarkDao.updateRecordedBark(bark)
    }

    suspend fun updateAiTranslation(
        id: Long,
        translation: String,
        emotion: String = "Alert",
        dogThought: String = ""
    ) {
        recordedBarkDao.updateAiTranslation(id, translation, emotion, dogThought)
    }

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        recordedBarkDao.setFavorite(id, isFavorite)
    }

    suspend fun deleteRecordedBark(bark: RecordedBark) {
        recordedBarkDao.deleteRecordedBark(bark)
    }

    suspend fun deleteRecordedBarkById(id: Long) {
        recordedBarkDao.deleteRecordedBarkById(id)
    }

    suspend fun clearAllRecordedBarks() {
        recordedBarkDao.clearAllRecordedBarks()
    }
}
