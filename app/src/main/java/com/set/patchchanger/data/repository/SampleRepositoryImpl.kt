package com.set.patchchanger.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import com.set.patchchanger.data.local.dao.AudioLibraryDao
import com.set.patchchanger.data.local.entities.AudioLibraryEntity
import com.set.patchchanger.data.local.dao.SampleDao
import com.set.patchchanger.data.local.entities.SampleEntity
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.repository.AudioLibraryRepository
import com.set.patchchanger.domain.repository.SampleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

    // SoundPool for low-latency playback
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
            // Unload sound from pool if it exists
            it.audioFileName?.let { fileName ->
                val fullPath = File(context.filesDir, fileName).absolutePath
                loadedSoundIds[fullPath]?.let { soundId ->
                    soundPool.unload(soundId)
                    loadedSoundIds.remove(fullPath)
                }
                // Delete the file
                File(fullPath).delete()
            }
            // Update DB
            sampleDao.updateSample(
                it.copy(
                    audioFileName = null,
                    sourceName = null,
                    name = "S${sampleId + 1}"
                )
            )
        }
        stopSample(sampleId)
    }

    override suspend fun saveSampleAudioFromUri(
        sampleId: Int,
        sourceUri: Uri,
        originalName: String
    ): String = withContext(Dispatchers.IO) {
        val fileName = "sample_${sampleId}_${System.currentTimeMillis()}"
        val destFile = File(context.filesDir, fileName)

        // Copy file from URI to internal storage
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        updateSampleDb(sampleId, fileName, originalName)
        loadSound(destFile.absolutePath)
        return@withContext fileName
    }

    override suspend fun saveSampleAudioFromLibrary(
        sampleId: Int,
        libraryItem: AudioLibraryItem
    ): String = withContext(Dispatchers.IO) {
        val fileName = "sample_${sampleId}_${System.currentTimeMillis()}"
        val destFile = File(context.filesDir, fileName)
        val sourceFile = File(context.filesDir, libraryItem.filePath)

        sourceFile.copyTo(destFile, overwrite = true)

        updateSampleDb(sampleId, fileName, libraryItem.name)
        loadSound(destFile.absolutePath)
        return@withContext fileName
    }


    private suspend fun updateSampleDb(sampleId: Int, fileName: String, sourceName: String) {
        val current = sampleDao.getSampleById(sampleId)
        val newEntity = current?.copy(
            audioFileName = fileName,
            sourceName = sourceName,
            name = sourceName.substringBeforeLast('.') // Set name to file name
        ) ?: SampleEntity(
            sampleId,
            sourceName.substringBeforeLast('.'),
            80,
            false,
            getDefaultSampleColors()[sampleId],
            fileName,
            sourceName
        )
        sampleDao.updateSample(newEntity)
    }

    override suspend fun resetSamples() {
        // Delete all audio files
        getSamples().forEach { clearSampleAudio(it.id) }
        // Delete from DB
        sampleDao.deleteAll()
        sampleDao.insertSamples(generateDefaultSamples())
        // Clear SoundPool cache
        loadedSoundIds.values.forEach { soundPool.unload(it) }
        loadedSoundIds.clear()
    }

    // --- Audio Playback Logic ---

    override suspend fun triggerSampleAudio(sampleId: Int) {
        val sample = sampleDao.getSampleById(sampleId)?.toDomain() ?: return
        if (sample.audioFileName == null) return

        // Stop existing stream for this pad if any (monophonic per pad)
        stopSample(sampleId)

        val fullPath = File(context.filesDir, sample.audioFileName).absolutePath
        val soundId = loadedSoundIds[fullPath] ?: soundPool.load(fullPath, 1)

        // Store ID if newly loaded
        if (!loadedSoundIds.containsKey(fullPath)) {
            loadedSoundIds[fullPath] = soundId
            // We should wait for onLoadComplete, but for low latency we play immediately
            // This might fail on first tap if file is large
        }

        val vol = sample.volume / 100f
        val loopCount = if (sample.loop) -1 else 0

        // Play
        val streamId = soundPool.play(soundId, vol, vol, 1, loopCount, 1f)
        if (streamId != 0) {
            activeStreamIds[sampleId] = streamId
        }
    }

    override fun stopSample(sampleId: Int) {
        activeStreamIds[sampleId]?.let {
            soundPool.stop(it)
            activeStreamIds.remove(sampleId)
        }
    }

    override fun cleanup() {
        soundPool.release()
    }

    private fun loadSound(path: String) {
        if (!loadedSoundIds.containsKey(path)) {
            loadedSoundIds[path] = soundPool.load(path, 1)
        }
    }

    private fun getDefaultSampleColors() = listOf("#008B8B", "#F50057", "#00C853", "#D500F9")

    private fun generateDefaultSamples(): List<SampleEntity> {
        val colors = getDefaultSampleColors()
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

    override suspend fun addAudioFile(sourceUri: Uri, originalName: String): AudioLibraryItem =
        withContext(Dispatchers.IO) {
            val fileName = "lib_${System.currentTimeMillis()}_$originalName"
            val destFile = File(context.filesDir, fileName)

            // Copy file from URI to internal storage
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

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
            return@withContext entity.toDomain()
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
    private fun SampleEntity.toDomain() =
        SamplePad(id, name, volume, loop, color, audioFileName, sourceName)

    private fun SamplePad.toEntity() =
        SampleEntity(id, name, volume, loop, color, audioFileName, sourceName)

    private fun AudioLibraryEntity.toDomain() =
        AudioLibraryItem(name, filePath, sizeBytes, durationMs, addedTimestamp)

    private fun AudioLibraryItem.toEntity() =
        AudioLibraryEntity(name, filePath, sizeBytes, durationMs, addedTimestamp)
}