package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.ai.AvatarGenerationState
import com.example.ai.DogAiTranslator
import com.example.ai.DogTranslationResult
import com.example.ai.PetAvatarGenerator
import com.example.ai.TranslationState
import com.example.audio.AcousticAnalysis
import com.example.audio.AudioProcessor
import com.example.audio.CanineSoundboardPlayer
import com.example.audio.SoundboardItem
import com.example.data.AppDatabase
import com.example.data.PetProfile
import com.example.data.TranslationLog
import com.example.util.CanineTtsPlayer
import com.example.util.NotificationHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BarkTalkViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val petProfileRepository = com.example.data.PetProfileRepository(database.petProfileDao())
    val translationRepository = com.example.data.TranslationRepository(database.translationDao())
    val recordedBarkRepository = com.example.data.RecordedBarkRepository(database.recordedBarkDao())

    private val audioProcessor = AudioProcessor()
    private val soundboardPlayer = CanineSoundboardPlayer()
    private val barkMediaRecorder = com.example.audio.BarkMediaRecorder(application)
    private val barkAudioPlayer = com.example.audio.BarkAudioPlayer()
    private val dogAiTranslator = DogAiTranslator()
    private val petAvatarGenerator = PetAvatarGenerator()
    private val notificationHelper = NotificationHelper(application)
    private val ttsPlayer = CanineTtsPlayer(application)

    val petProfiles: StateFlow<List<PetProfile>> = petProfileRepository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPet: StateFlow<PetProfile?> = petProfileRepository.selectedProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val translationLogs: StateFlow<List<TranslationLog>> = translationRepository.allTranslations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTranslations: StateFlow<List<TranslationLog>> = translationRepository.favoriteTranslations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recordedBarks: StateFlow<List<com.example.data.RecordedBark>> = recordedBarkRepository.allRecordedBarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRecordedBarks: StateFlow<List<com.example.data.RecordedBark>> = recordedBarkRepository.favoriteRecordedBarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastTranslatedLogId = MutableStateFlow<Long?>(null)
    val lastTranslatedLogId: StateFlow<Long?> = _lastTranslatedLogId.asStateFlow()

    val isListening: StateFlow<Boolean> = audioProcessor.isRecording
    val liveVolumeDb: StateFlow<Float> = audioProcessor.liveVolumeDb
    val livePitchHz: StateFlow<Int> = audioProcessor.livePitchHz
    val liveWaveform: StateFlow<FloatArray> = audioProcessor.liveWaveform

    // MediaRecorder states for audio sample capture
    val isMediaRecording: StateFlow<Boolean> = barkMediaRecorder.isRecording
    val mediaRecordingDurationSeconds: StateFlow<Float> = barkMediaRecorder.recordingDurationSeconds
    val mediaRecorderVolumeDb: StateFlow<Float> = barkMediaRecorder.liveVolumeDb
    val mediaRecorderWaveform: StateFlow<FloatArray> = barkMediaRecorder.liveWaveform
    val currentlyPlayingBarkPath: StateFlow<String?> = barkAudioPlayer.currentlyPlayingPath

    private val _lastRecordedSample = MutableStateFlow<com.example.audio.RecordedBarkSample?>(null)
    val lastRecordedSample: StateFlow<com.example.audio.RecordedBarkSample?> = _lastRecordedSample.asStateFlow()

    val currentlyPlayingSoundId: StateFlow<String?> = soundboardPlayer.currentlyPlayingId
    val lastPlayedSound: StateFlow<SoundboardItem?> = soundboardPlayer.lastPlayedSound

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    private val _avatarGenState = MutableStateFlow<AvatarGenerationState>(AvatarGenerationState.Idle)
    val avatarGenState: StateFlow<AvatarGenerationState> = _avatarGenState.asStateFlow()

    private val prefs = application.getSharedPreferences("barktalk_prefs", android.content.Context.MODE_PRIVATE)

    private val _dailyBarkGoal = MutableStateFlow(prefs.getInt("daily_bark_goal", 5).coerceIn(1, 50))
    val dailyBarkGoal: StateFlow<Int> = _dailyBarkGoal.asStateFlow()

    private val _autoSpeakTranslations = MutableStateFlow(true)
    val autoSpeakTranslations: StateFlow<Boolean> = _autoSpeakTranslations.asStateFlow()

    private val _notifyHighUrgency = MutableStateFlow(true)
    val notifyHighUrgency: StateFlow<Boolean> = _notifyHighUrgency.asStateFlow()

    fun setDailyBarkGoal(goal: Int) {
        val clamped = goal.coerceIn(1, 50)
        _dailyBarkGoal.value = clamped
        prefs.edit().putInt("daily_bark_goal", clamped).apply()
    }

    fun toggleAutoSpeak() {
        _autoSpeakTranslations.value = !_autoSpeakTranslations.value
    }

    fun toggleNotifyHighUrgency() {
        _notifyHighUrgency.value = !_notifyHighUrgency.value
    }

    fun startListening() {
        audioProcessor.startListening(viewModelScope) { analysis ->
            translateVocalization(analysis)
        }
    }

    fun stopListening() {
        audioProcessor.stopListening()
    }

    fun startMediaRecording(): Boolean {
        if (audioProcessor.isRecording.value) {
            audioProcessor.stopListening()
        }
        return barkMediaRecorder.startRecording(viewModelScope)
    }

    fun stopMediaRecording() {
        val sample = barkMediaRecorder.stopRecording()
        if (sample != null) {
            _lastRecordedSample.value = sample
            translateVocalization(
                analysis = sample.acousticAnalysis,
                audioFilePath = sample.audioFile.absolutePath
            )
        }
    }

    fun stopMediaRecordingForPreparation(): com.example.audio.RecordedBarkSample? {
        val sample = barkMediaRecorder.stopRecording()
        if (sample != null) {
            _lastRecordedSample.value = sample
        }
        return sample
    }

    fun submitPreparedSampleForAnalysis(
        sample: com.example.audio.RecordedBarkSample,
        targetPet: PetProfile? = null
    ) {
        if (targetPet != null && targetPet.id > 0L) {
            selectPet(targetPet.id)
        }
        translateVocalization(
            analysis = sample.acousticAnalysis,
            audioFilePath = sample.audioFile.absolutePath,
            overridePet = targetPet
        )
    }

    fun discardRecordedSample() {
        stopPlayingRecordedBark()
        _lastRecordedSample.value = null
    }

    fun playRecordedBark(filePath: String) {
        barkAudioPlayer.playAudioFile(filePath)
    }

    fun stopPlayingRecordedBark() {
        barkAudioPlayer.stop()
    }

    fun translateVocalization(
        analysis: AcousticAnalysis,
        audioFilePath: String? = null,
        overridePet: PetProfile? = null
    ) {
        val currentPet = overridePet ?: selectedPet.value ?: petProfiles.value.firstOrNull() ?: PetProfile(
            name = "Barnaby",
            breed = "Golden Retriever",
            ageYears = 3,
            ageMonths = 4
        )

        viewModelScope.launch {
            _translationState.value = TranslationState.TranslatingWithAi
            try {
                val result = dogAiTranslator.translateCanineVocalization(currentPet, analysis)
                _translationState.value = TranslationState.Success(result, analysis)

                val nowTimestamp = System.currentTimeMillis()
                // Save to Room history with optional captured audio file
                val newLog = TranslationLog(
                    petId = currentPet.id,
                    petName = currentPet.name,
                    petBreed = currentPet.breed,
                    timestamp = nowTimestamp,
                    vocalizationType = analysis.vocalizationType,
                    dominantPitchHz = analysis.pitchHz,
                    intensityDb = analysis.volumeDb,
                    cadenceDescription = analysis.cadenceDescription,
                    englishTranslation = result.englishTranslation,
                    dogThought = result.dogThought,
                    detectedEmotion = result.detectedEmotion,
                    urgencyLevel = result.urgencyLevel,
                    confidencePercent = result.confidencePercent,
                    recommendedAction = result.recommendedAction,
                    canineSubtext = result.canineSubtext,
                    isFavorite = false,
                    audioFilePath = audioFilePath
                )
                val logId = translationRepository.insertTranslation(newLog)
                _lastTranslatedLogId.value = logId

                // Also persist to RecordedBark Room table with timestamp and AI-generated translation
                val recordedDuration = _lastRecordedSample.value?.durationSeconds ?: 1.8f
                val recordedBark = com.example.data.RecordedBark(
                    timestamp = nowTimestamp,
                    translation = result.englishTranslation,
                    audioFilePath = audioFilePath,
                    durationSeconds = recordedDuration,
                    peakDb = analysis.volumeDb,
                    dominantPitchHz = analysis.pitchHz,
                    vocalizationType = analysis.vocalizationType,
                    cadenceDescription = analysis.cadenceDescription,
                    detectedEmotion = result.detectedEmotion,
                    dogThought = result.dogThought,
                    urgencyLevel = result.urgencyLevel,
                    confidencePercent = result.confidencePercent,
                    petId = currentPet.id,
                    petName = currentPet.name,
                    petBreed = currentPet.breed,
                    isFavorite = false
                )
                recordedBarkRepository.insertRecordedBark(recordedBark)

                com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())

                // Trigger push notification if HIGH or CRITICAL
                if (_notifyHighUrgency.value && (result.urgencyLevel == "HIGH" || result.urgencyLevel == "CRITICAL")) {
                    notificationHelper.showUrgentBarkAlert(newLog.copy(id = logId))
                }

                // Text-to-speech out loud
                if (_autoSpeakTranslations.value) {
                    ttsPlayer.speak(result.englishTranslation)
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _translationState.value = TranslationState.Error(
                    e.localizedMessage ?: "Failed to interpret bark. Please try again."
                )
            }
        }
    }

    fun simulateBarkScenario(scenario: AcousticAnalysis) {
        translateVocalization(scenario)
    }

    fun playSoundboard(item: SoundboardItem) {
        soundboardPlayer.playSound(item, viewModelScope)
    }

    fun stopSoundboard() {
        soundboardPlayer.stop()
    }

    fun speakTranslation(text: String) {
        ttsPlayer.speak(text)
    }

    fun stopSpeaking() {
        ttsPlayer.stop()
    }

    fun toggleFavorite(log: TranslationLog) {
        viewModelScope.launch {
            val newFavorite = !log.isFavorite
            translationRepository.setFavorite(log.id, newFavorite)
            // Keep companion RecordedBark table in sync if a matching timestamp exists
            recordedBarks.value
                .filter { kotlin.math.abs(it.timestamp - log.timestamp) <= 2000L }
                .forEach { matchingBark ->
                    recordedBarkRepository.setFavorite(matchingBark.id, newFavorite)
                }
        }
    }

    fun toggleLatestTranslationFavorite() {
        val targetId = _lastTranslatedLogId.value
        val targetLog = if (targetId != null) {
            translationLogs.value.find { it.id == targetId }
        } else {
            translationLogs.value.firstOrNull()
        }
        if (targetLog != null) {
            toggleFavorite(targetLog)
        }
    }

    fun deleteLog(log: TranslationLog) {
        viewModelScope.launch {
            translationRepository.deleteTranslation(log)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteRecordedBark(bark: com.example.data.RecordedBark) {
        viewModelScope.launch {
            recordedBarkRepository.deleteRecordedBark(bark)
        }
    }

    fun toggleRecordedBarkFavorite(bark: com.example.data.RecordedBark) {
        viewModelScope.launch {
            val newFavorite = !bark.isFavorite
            recordedBarkRepository.setFavorite(bark.id, newFavorite)
            translationLogs.value
                .filter { kotlin.math.abs(it.timestamp - bark.timestamp) <= 2000L }
                .forEach { matchingLog ->
                    translationRepository.setFavorite(matchingLog.id, newFavorite)
                }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            translationRepository.clearHistory()
            recordedBarkRepository.clearAllRecordedBarks()
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun selectPet(petId: Long) {
        viewModelScope.launch {
            petProfileRepository.selectProfile(petId)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun addPetProfile(profile: PetProfile) {
        viewModelScope.launch {
            petProfileRepository.insertAndSelectProfile(profile)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun updatePetProfile(profile: PetProfile) {
        viewModelScope.launch {
            petProfileRepository.updateProfile(profile)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deletePetProfile(profile: PetProfile) {
        viewModelScope.launch {
            petProfileRepository.deleteProfileAndEnsureSelection(profile)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun sendTestNotification() {
        val petName = selectedPet.value?.name ?: "Barnaby"
        notificationHelper.sendTestAlert(petName)
    }

    fun resetTranslationState() {
        _translationState.value = TranslationState.Idle
    }

    fun generatePetAvatar(
        pet: PetProfile,
        onAvatarSaved: ((String) -> Unit)? = null
    ) {
        val targetPetId = pet.id.takeIf { it > 0L }
        val displayName = pet.name.trim().ifBlank { "Your Dog" }
        _avatarGenState.value = AvatarGenerationState.Generating(
            petId = targetPetId,
            petName = displayName
        )

        viewModelScope.launch {
            try {
                val bitmap = petAvatarGenerator.generateDigitalAvatarBitmap(pet)
                val savedUri = withContext(Dispatchers.IO) {
                    PetAvatarGenerator.saveAvatarBitmapToFile(
                        context = getApplication(),
                        bitmap = bitmap,
                        petName = displayName
                    )
                }

                if (pet.id > 0L) {
                    petProfileRepository.updateProfile(pet.copy(photoUri = savedUri))
                    com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
                }

                onAvatarSaved?.invoke(savedUri)
                _avatarGenState.value = AvatarGenerationState.Success(
                    petId = targetPetId,
                    avatarUri = savedUri,
                    bitmap = bitmap,
                    petName = displayName
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BarkTalkViewModel", "Failed to generate Gemini digital pet avatar", e)
                _avatarGenState.value = AvatarGenerationState.Error(
                    petId = targetPetId,
                    message = e.localizedMessage ?: "Failed to generate digital avatar with Gemini. Please try again."
                )
            }
        }
    }

    fun resetAvatarGenerationState() {
        _avatarGenState.value = AvatarGenerationState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        audioProcessor.stopListening()
        soundboardPlayer.stop()
        barkAudioPlayer.stop()
        ttsPlayer.release()
    }
}
