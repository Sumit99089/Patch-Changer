package com.set.patchchanger.data.local

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Map of SampleID to its dedicated ExoPlayer instance
    private val playerPool = mutableMapOf<Int, ExoPlayer>()

    // Callback to notify when a sample finishes playing naturally
    private var onPlaybackEnded: ((Int) -> Unit)? = null

    fun setPlaybackEndedListener(listener: (Int) -> Unit) {
        onPlaybackEnded = listener
    }

    private fun getOrCreatePlayer(sampleId: Int): ExoPlayer {
        return playerPool.getOrPut(sampleId) {
            ExoPlayer.Builder(context).build().apply {
                // This ensures the audio starts exactly when the previous one ends
                repeatMode = Player.REPEAT_MODE_OFF

                // Add listener to detect when playback finishes naturally
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            onPlaybackEnded?.invoke(sampleId)
                        }
                    }
                })
            }
        }
    }

    fun playSound(sampleId: Int, filePath: String?, volume: Int, loop: Boolean) {
        if (filePath == null) return

        val player = getOrCreatePlayer(sampleId)

        // Prepare the media item from the local file path
        val uri = Uri.parse(filePath)
        val mediaItem = MediaItem.fromUri(uri)

        player.apply {
            stop() // Stop current playback if any
            clearMediaItems()
            setMediaItem(mediaItem)

            // Set looping and volume
            repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            setVolume(volume / 100f)

            prepare()
            playWhenReady = true
        }
    }

    fun stopSound(sampleId: Int) {
        playerPool[sampleId]?.stop()
    }

    fun cleanup() {
        playerPool.values.forEach {
            it.release()
        }
        playerPool.clear()
    }
}