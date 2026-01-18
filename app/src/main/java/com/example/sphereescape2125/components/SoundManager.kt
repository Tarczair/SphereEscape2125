package com.example.sphereescape2125

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

object SoundManager {
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var popSoundId: Int = 0

    // Głośność (domyślne wartości startowe)
    var musicVolume: Float = 0.5f
        set(value) {
            field = value
            updateMusicVolume()
        }

    var sfxVolume: Float = 0.7f

    fun init(context: Context) {
        // 1. Konfiguracja SoundPool dla efektów (pop.mp3)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Ładujemy plik pop.mp3
        popSoundId = soundPool?.load(context, R.raw.pop, 1) ?: 0

        // 2. Konfiguracja MediaPlayer dla muzyki (bmusic.mp3)
        mediaPlayer = MediaPlayer.create(context, R.raw.bmusic)
        mediaPlayer?.isLooping = true // Muzyka w pętli
        updateMusicVolume()
    }

    fun playMusic() {
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun playPopSound() {
        // Odtwórz dźwięk pop z aktualną głośnością efektów
        soundPool?.play(popSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    private fun updateMusicVolume() {
        mediaPlayer?.setVolume(musicVolume, musicVolume)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        soundPool?.release()
        soundPool = null
    }
}