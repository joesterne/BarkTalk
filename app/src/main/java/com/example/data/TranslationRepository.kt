package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository pattern implementation for Canine Communication History.
 * Abstracts Room database queries from ViewModels and UI layers.
 */
class TranslationRepository(private val translationDao: TranslationDao) {

    /**
     * Reactive stream of all past translations, ordered by timestamp descending.
     */
    val allTranslations: Flow<List<TranslationLog>> = translationDao.getAllTranslations()

    /**
     * Reactive stream of favorite translations.
     */
    val favoriteTranslations: Flow<List<TranslationLog>> = translationDao.getFavoriteLogs()

    /**
     * Reactive stream of the most recent translation.
     */
    val latestTranslation: Flow<TranslationLog?> = translationDao.getLatestLog()

    /**
     * Total translation count stream.
     */
    val translationCount: Flow<Int> = translationDao.getTranslationCount()

    fun getTranslationsForPet(petId: Long): Flow<List<TranslationLog>> {
        return translationDao.getLogsForPet(petId)
    }

    fun searchTranslations(query: String): Flow<List<TranslationLog>> {
        return translationDao.searchTranslations(query)
    }

    fun getTranslationsByEmotion(emotion: String): Flow<List<TranslationLog>> {
        return translationDao.getTranslationsByEmotion(emotion)
    }

    fun getTranslationsByMinIntensity(minDb: Int): Flow<List<TranslationLog>> {
        return translationDao.getTranslationsByMinIntensity(minDb)
    }

    suspend fun getTranslationById(id: Long): TranslationLog? {
        return translationDao.getLogById(id)
    }

    suspend fun insertTranslation(translation: TranslationLog): Long {
        return translationDao.insertLog(translation)
    }

    suspend fun insertTranslations(translations: List<TranslationLog>): List<Long> {
        return translationDao.insertTranslations(translations)
    }

    suspend fun updateTranslation(translation: TranslationLog) {
        translationDao.updateLog(translation)
    }

    suspend fun deleteTranslation(translation: TranslationLog) {
        translationDao.deleteLog(translation)
    }

    suspend fun deleteTranslationById(id: Long) {
        translationDao.deleteLogById(id)
    }

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        translationDao.setFavorite(id, isFavorite)
    }

    suspend fun clearHistory() {
        translationDao.clearAllLogs()
    }
}
