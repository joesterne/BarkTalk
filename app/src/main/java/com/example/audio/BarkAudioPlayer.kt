package com.example.audio

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class BarkAudioPlayer {
    private val TAG = "BarkAudioPlayer"
    private var mediaPlayer: MediaPlayer? = null

    private val _currentlyPlayingPath = MutableStateFlow<String?>(null)
    val currentlyPlayingPath: StateFlow<String?> = _currentlyPlayingPath.asStateFlow()

    fun playAudioFile(filePath: String, onCompletion: (() -> Unit)? = null) {
        stop()
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "Audio file does not exist: $filePath")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                _currentlyPlayingPath.value = filePath
                setOnCompletionListener {
                    stop()
                    onCompletion?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    stop()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing audio file: $filePath", e)
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaPlayer", e)
        }
        mediaPlayer = null
        _currentlyPlayingPath.value = null
    }

    fun isPlaying(filePath: String): Boolean {
        return _currentlyPlayingPath.value == filePath
    }
}
