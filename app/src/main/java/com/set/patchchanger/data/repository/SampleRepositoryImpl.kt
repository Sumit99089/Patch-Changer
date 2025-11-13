package com.set.patchchanger.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import com.set.patchchanger.data.local.AudioLibraryDao
import com.set.patchchanger.data.local.AudioLibraryEntity
import com.set.patchchanger.data.local.SampleDao
import com.set.patchchanger.data.local.SampleEntity
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.repository.AudioLibraryRepository
import com.set.patchchanger.domain.repository.SampleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleRepositoryImpl @Inject constructor(
    private val sampleDao: SampleDao,
    private val audioLibraryDao: AudioLibraryDao,
    @ApplicationContext private val context: Context
) : SampleRepository, AudioLibraryRepository {

    // SoundPool for low-latency playback (like the HTML AudioContext)
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSoundIds = mutableMapOf<String, Int>() // FilePath -> SoundID
    private val activeStreamIds = mutableMapOf<Int, Int>()   // SampleID -> StreamID

    override fun observeSamples(): Flow<List<SamplePad>> {
        return sampleDao.observeAllSamples().map { entities ->
            if (entities.isEmpty()) {
                // Initialize defaults if empty
                val defaults = generateDefaultSamples()
                sampleDao.insertSamples(defaults)
                defaults.map { it.toDomain() }
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun getSamples(): List<SamplePad> {
        return sampleDao.getAllSamples().map { it.toDomain() }
    }

    override suspend fun updateSample(sample: SamplePad) {
        sampleDao.updateSample(sample.toEntity())
        // Preload sound if needed
        sample.audioFileName?.let { fileName ->
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                loadSound(file.absolutePath)
            }
        }
    }

    override suspend fun clearSampleAudio(sampleId: Int) {
        val current = sampleDao.getSampleById(sampleId)
        current?.let {
            sampleDao.updateSample(it.copy(audioFileName = null, sourceName = null))
        }
        stopSample(sampleId)
    }

    override suspend fun saveSampleAudio(sampleId: Int, sourceFile: File): String {
        val fileName = "sample_${sampleId}_${System.currentTimeMillis()}.wav"
        val destFile = File(context.filesDir, fileName)
        sourceFile.copyTo(destFile, overwrite = true)

        // Update DB
        val current = sampleDao.getSampleById(sampleId)
        val newEntity = current?.copy(
            audioFileName = fileName,
            sourceName = sourceFile.name
        ) ?: SampleEntity(sampleId, "S${sampleId+1}", 80, false, "#008B8B", fileName, sourceFile.name)

        sampleDao.insertSamples(listOf(newEntity))
        loadSound(destFile.absolutePath)
        return fileName
    }

    override suspend fun resetSamples() {
        sampleDao.deleteAll()
        sampleDao.insertSamples(generateDefaultSamples())
        // Clear SoundPool cache
        loadedSoundIds.values.forEach { soundPool.unload(it) }
        loadedSoundIds.clear()
    }

    // --- Audio Playback Logic ---

    fun playSample(sampleId: Int) {
        // Stop existing stream for this pad if any (monophonic per pad)
        stopSample(sampleId)

        // We need to fetch the latest config synchronously or cache it.
        // For simplicity, we rely on the caller or internal cache.
        // In a real app, you might want a RAM cache for `sampleConfigs`.
        // Here we assume the file path is passed or looked up.
    }

    // Helper to actually play sound given specific parameters (called from UI/ViewModel)
    fun triggerSampleAudio(filePath: String, volume: Int, loop: Boolean, sampleId: Int) {
        val fullPath = File(context.filesDir, filePath).absolutePath
        val soundId = loadedSoundIds[fullPath] ?: soundPool.load(fullPath, 1)

        // Store ID if newly loaded
        if (!loadedSoundIds.containsKey(fullPath)) {
            loadedSoundIds[fullPath] = soundId
            // Note: soundPool.load is async. In a perfect world we wait for onLoadComplete.
            // For now, we assume small samples load fast.
        }

        val vol = volume / 100f
        val loopCount = if (loop) -1 else 0

        // Play
        val streamId = soundPool.play(soundId, vol, vol, 1, loopCount, 1f)
        activeStreamIds[sampleId] = streamId
    }

    fun stopSample(sampleId: Int) {
        activeStreamIds[sampleId]?.let {
            soundPool.stop(it)
            activeStreamIds.remove(sampleId)
        }
    }

    private fun loadSound(path: String) {
        if (!loadedSoundIds.containsKey(path)) {
            loadedSoundIds[path] = soundPool.load(path, 1)
        }
    }

    private fun generateDefaultSamples(): List<SampleEntity> {
        val colors = listOf("#008B8B", "#F50057", "#00C853", "#D500F9") // From HTML
        return (0..3).map { i ->
            SampleEntity(
                id = i,
                name = "S${i + 1}",
                volume = 80,
                loop = false,
                color = colors[i],
                audioFileName = null,
                sourceName = null
            )
        }
    }

    // --- Audio Library Implementation ---

    override fun observeLibrary(): Flow<List<AudioLibraryItem>> {
        return audioLibraryDao.observeAllAudio().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getLibraryItems(): List<AudioLibraryItem> {
        return audioLibraryDao.getAllAudio().map { it.toDomain() }
    }

    override suspend fun addAudioFile(sourceFile: File, originalName: String): AudioLibraryItem {
        val fileName = "lib_${System.currentTimeMillis()}_${originalName}"
        val destFile = File(context.filesDir, fileName)
        sourceFile.copyTo(destFile, overwrite = true)

        // Use MediaMetadataRetriever for duration if needed, skipping for brevity
        val duration = 0L

        val entity = AudioLibraryEntity(
            name = originalName,
            filePath = fileName,
            sizeBytes = destFile.length(),
            durationMs = duration,
            addedTimestamp = System.currentTimeMillis()
        )
        audioLibraryDao.insertAudio(entity)
        return entity.toDomain()
    }

    override suspend fun deleteAudioFile(item: AudioLibraryItem) {
        val file = File(context.filesDir, item.filePath)
        if (file.exists()) file.delete()
        audioLibraryDao.deleteAudio(item.toEntity())
    }

    override fun getAudioFile(item: AudioLibraryItem): File {
        return File(context.filesDir, item.filePath)
    }

    override suspend fun searchLibrary(query: String): List<AudioLibraryItem> {
        return audioLibraryDao.searchAudio(query).map { it.toDomain() }
    }

    // Mappers
    private fun SampleEntity.toDomain() = SamplePad(id, name, volume, loop, color, audioFileName, sourceName)
    private fun SamplePad.toEntity() = SampleEntity(id, name, volume, loop, color, audioFileName, sourceName)
    private fun AudioLibraryEntity.toDomain() = AudioLibraryItem(name, filePath, sizeBytes, durationMs, addedTimestamp)
    private fun AudioLibraryItem.toEntity() = AudioLibraryEntity(name, filePath, sizeBytes, durationMs, addedTimestamp)
}