package com.set.patchchanger.data.local

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSounds = mutableMapOf<String, Int>() // FilePath -> SoundID
    private val activeStreams = mutableMapOf<Int, Int>()   // SampleID -> StreamID

    fun loadSound(filePath: String) {
        if (!loadedSounds.containsKey(filePath)) {
            // In a real app, you might need to handle Uri permissions or copy to internal storage
            // This assumes filePath is a valid accessible path or Uri string
            val soundId = soundPool.load(filePath, 1)
            loadedSounds[filePath] = soundId
        }
    }

    fun playSound(sampleId: Int, filePath: String?, volume: Int, loop: Boolean) {
        if (filePath == null) return

        // Stop existing if playing
        activeStreams[sampleId]?.let { soundPool.stop(it) }

        val soundId = loadedSounds[filePath] ?: run {
            // Attempt load if not cached (might delay first play)
            val newId = soundPool.load(filePath, 1)
            loadedSounds[filePath] = newId
            newId
        }

        // Note: soundPool.load is async. In production, handle onLoadComplete.

        val vol = volume / 100f
        val loopCount = if (loop) -1 else 0

        val streamId = soundPool.play(soundId, vol, vol, 1, loopCount, 1.0f)
        activeStreams[sampleId] = streamId
    }

    fun stopSound(sampleId: Int) {
        activeStreams[sampleId]?.let {
            soundPool.stop(it)
            activeStreams.remove(sampleId)
        }
    }

    fun cleanup() {
        soundPool.release()
    }
}