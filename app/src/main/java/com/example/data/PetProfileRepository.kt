package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository pattern implementation for local Canine Pet Profiles.
 * Abstracts Room database queries and atomic selection transactions from ViewModels and UI layers.
 */
class PetProfileRepository(private val petProfileDao: PetProfileDao) {

    /**
     * Reactive stream of all persisted pet profiles, ordered with the active pet first.
     */
    val allProfiles: Flow<List<PetProfile>> = petProfileDao.getAllProfiles()

    /**
     * Reactive stream of the currently selected active dog profile.
     */
    val selectedProfile: Flow<PetProfile?> = petProfileDao.getSelectedProfile()

    /**
     * Reactive stream of the total number of saved pet profiles.
     */
    val profileCount: Flow<Int> = petProfileDao.getProfileCount()

    suspend fun getProfileById(id: Long): PetProfile? {
        return petProfileDao.getProfileById(id)
    }

    suspend fun getAllProfilesOnce(): List<PetProfile> {
        return petProfileDao.getAllProfilesOnce()
    }

    suspend fun insertProfile(profile: PetProfile): Long {
        return petProfileDao.insertProfile(profile)
    }

    suspend fun insertAndSelectProfile(profile: PetProfile): Long {
        return petProfileDao.insertAndSelectProfile(profile)
    }

    suspend fun updateProfile(profile: PetProfile) {
        petProfileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: PetProfile) {
        petProfileDao.deleteProfile(profile)
    }

    suspend fun selectProfile(petId: Long) {
        petProfileDao.setSelectedProfile(petId)
    }

    suspend fun deleteProfileAndEnsureSelection(profile: PetProfile) {
        petProfileDao.deleteProfileAndEnsureSelection(profile)
    }
}
