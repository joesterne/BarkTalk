package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DogAiTranslator
import com.example.ai.DogTranslationResult
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BarkTalkViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val petDao = database.petProfileDao()
    private val translationDao = database.translationDao()
    val translationRepository = com.example.data.TranslationRepository(translationDao)

    private val audioProcessor = AudioProcessor()
    private val soundboardPlayer = CanineSoundboardPlayer()
    private val barkMediaRecorder = com.example.audio.BarkMediaRecorder(application)
    private val barkAudioPlayer = com.example.audio.BarkAudioPlayer()
    private val dogAiTranslator = DogAiTranslator()
    private val notificationHelper = NotificationHelper(application)
    private val ttsPlayer = CanineTtsPlayer(application)

    val petProfiles: StateFlow<List<PetProfile>> = petDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPet: StateFlow<PetProfile?> = petDao.getSelectedProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val translationLogs: StateFlow<List<TranslationLog>> = translationRepository.allTranslations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    private val _autoSpeakTranslations = MutableStateFlow(true)
    val autoSpeakTranslations: StateFlow<Boolean> = _autoSpeakTranslations.asStateFlow()

    private val _notifyHighUrgency = MutableStateFlow(true)
    val notifyHighUrgency: StateFlow<Boolean> = _notifyHighUrgency.asStateFlow()

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

    fun playRecordedBark(filePath: String) {
        barkAudioPlayer.playAudioFile(filePath)
    }

    fun stopPlayingRecordedBark() {
        barkAudioPlayer.stop()
    }

    fun translateVocalization(analysis: AcousticAnalysis, audioFilePath: String? = null) {
        val currentPet = selectedPet.value ?: petProfiles.value.firstOrNull() ?: PetProfile(
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

                // Save to Room history with optional captured audio file
                val newLog = TranslationLog(
                    petId = currentPet.id,
                    petName = currentPet.name,
                    petBreed = currentPet.breed,
                    timestamp = System.currentTimeMillis(),
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
            translationRepository.setFavorite(log.id, !log.isFavorite)
        }
    }

    fun deleteLog(log: TranslationLog) {
        viewModelScope.launch {
            translationRepository.deleteTranslation(log)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            translationRepository.clearHistory()
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun selectPet(petId: Long) {
        viewModelScope.launch {
            petDao.clearSelection()
            petDao.selectProfile(petId)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun addPetProfile(profile: PetProfile) {
        viewModelScope.launch {
            val newId = petDao.insertProfile(profile)
            petDao.clearSelection()
            petDao.selectProfile(newId)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun updatePetProfile(profile: PetProfile) {
        viewModelScope.launch {
            petDao.updateProfile(profile)
            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deletePetProfile(profile: PetProfile) {
        viewModelScope.launch {
            petDao.deleteProfile(profile)
            val remaining = petDao.getAllProfiles()
            // If deleted was selected, select first available
            petProfiles.value.firstOrNull { it.id != profile.id }?.let {
                petDao.selectProfile(it.id)
            }
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

    override fun onCleared() {
        super.onCleared()
        audioProcessor.stopListening()
        soundboardPlayer.stop()
        barkAudioPlayer.stop()
        ttsPlayer.release()
    }
}
