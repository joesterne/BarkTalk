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

    /**
     * Reactive stream of distinct non-empty user-assigned dog tags.
     */
    val allDogTags: Flow<List<String>> = translationDao.getAllDogTags()

    /**
     * Reactive stream of all translation logs that have a user-assigned dog tag.
     */
    val taggedTranslations: Flow<List<TranslationLog>> = translationDao.getTaggedLogs()

    fun getTranslationsForPet(petId: Long): Flow<List<TranslationLog>> {
        return translationDao.getLogsForPet(petId)
    }

    fun getTranslationsByDogTag(dogTag: String): Flow<List<TranslationLog>> {
        return translationDao.getLogsByDogTag(dogTag)
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
        val normalizedTag = translation.dogTag?.trim()?.takeIf { it.isNotEmpty() }
        return translationDao.insertLog(translation.copy(dogTag = normalizedTag))
    }

    suspend fun insertTranslations(translations: List<TranslationLog>): List<Long> {
        val normalized = translations.map { log ->
            log.copy(dogTag = log.dogTag?.trim()?.takeIf { it.isNotEmpty() })
        }
        return translationDao.insertTranslations(normalized)
    }

    suspend fun updateTranslation(translation: TranslationLog) {
        val normalizedTag = translation.dogTag?.trim()?.takeIf { it.isNotEmpty() }
        translationDao.updateLog(translation.copy(dogTag = normalizedTag))
    }

    suspend fun updateDogTag(id: Long, dogTag: String?) {
        val normalizedTag = dogTag?.trim()?.takeIf { it.isNotEmpty() }
        translationDao.updateDogTag(id, normalizedTag)
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
