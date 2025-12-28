package com.set.patchchanger.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.set.patchchanger.data.local.AudioPlayer
import com.set.patchchanger.data.local.dao.AudioLibraryDao
import com.set.patchchanger.data.local.dao.SampleDao
import com.set.patchchanger.data.local.entities.AudioLibraryEntity
import com.set.patchchanger.data.local.entities.SampleEntity
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.domain.repository.AudioLibraryRepository
import com.set.patchchanger.domain.repository.SampleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleRepositoryImpl @Inject constructor(
    private val sampleDao: SampleDao,
    private val audioLibraryDao: AudioLibraryDao,
    private val audioPlayer: AudioPlayer, // Added the new AudioPlayer
    @param:ApplicationContext private val context: Context,
    private val scope: CoroutineScope
) : SampleRepository, AudioLibraryRepository {

    private val _playingSampleIds = MutableStateFlow<Set<Int>>(emptySet())
    override fun observePlayingStates(): Flow<Set<Int>> = _playingSampleIds.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            sampleDao.observeAllSamples().collect { entities ->
                if (entities.isEmpty()) {
                    sampleDao.insertSamples(generateDefaultSamples())
                }
            }
        }
    }

    override fun observeSamples(): Flow<List<SamplePad>> =
        sampleDao.observeAllSamples()
            .map { list -> if (list.isEmpty()) emptyList() else list.map { it.toDomain() } }

    override suspend fun getSamples(): List<SamplePad> =
        sampleDao.getAllSamples().map { it.toDomain() }

    override suspend fun updateSample(sample: SamplePad) {
        sampleDao.updateSample(sample.toEntity())
    }

    override suspend fun clearSampleAudio(sampleId: Int) {
        stopSample(sampleId)
        val current = sampleDao.getSampleById(sampleId)
        current?.let {
            sampleDao.updateSample(
                it.copy(
                    audioFileName = null,
                    sourceName = null,
                    name = "S${sampleId + 1}"
                )
            )
        }
    }

    override suspend fun saveSampleAudioFromUri(
        sampleId: Int,
        sourceUri: Uri,
        originalName: String
    ): String = withContext(Dispatchers.IO) {
        val fileName = "sample_${sampleId}_${System.currentTimeMillis()}"
        val destFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(sourceUri)
            ?.use { input -> FileOutputStream(destFile).use { input.copyTo(it) } }

        updateSampleDb(sampleId, fileName, originalName)
        return@withContext fileName
    }

    override suspend fun saveSampleAudioFromLibrary(
        sampleId: Int,
        libraryItem: AudioLibraryItem
    ): String = withContext(Dispatchers.IO) {
        val fileName = "sample_${sampleId}_${System.currentTimeMillis()}"
        val destFile = File(context.filesDir, fileName)
        val sourceFile = File(context.filesDir, libraryItem.filePath)
        if (sourceFile.exists()) {
            sourceFile.copyTo(destFile, overwrite = true)
            updateSampleDb(sampleId, fileName, libraryItem.name)
        }
        return@withContext fileName
    }

    private suspend fun updateSampleDb(sampleId: Int, fileName: String, sourceName: String) {
        val current = sampleDao.getSampleById(sampleId)
        val newEntity = current?.copy(
            audioFileName = fileName,
            sourceName = sourceName,
            name = sourceName.substringBeforeLast('.')
        )
            ?: SampleEntity(
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
        withContext(Dispatchers.Main) {
            // Stop all active audio using the player
            (0..3).forEach { stopSample(it) }
        }
        sampleDao.deleteAll()
        sampleDao.insertSamples(generateDefaultSamples())
    }

    override suspend fun triggerSampleAudio(sampleId: Int) {
        // 1. Get sample info from DB (IO Thread)
        val sample = withContext(Dispatchers.IO) {
            sampleDao.getSampleById(sampleId)?.toDomain()
        } ?: return

        if (sample.audioFileName == null) return
        val file = File(context.filesDir, sample.audioFileName)
        if (!file.exists()) return

        // 2. Manage AudioPlayer (Main Thread)
        withContext(Dispatchers.Main) {
            if (_playingSampleIds.value.contains(sampleId)) {
                stopSample(sampleId)
            } else {
                // Pass path to the ExoPlayer-based AudioPlayer
                audioPlayer.playSound(
                    sampleId = sampleId,
                    filePath = file.absolutePath,
                    volume = sample.volume,
                    loop = sample.loop
                )
                setPlayingState(sampleId, true)
            }
        }
    }

    override fun stopSample(sampleId: Int) {
        audioPlayer.stopSound(sampleId)
        setPlayingState(sampleId, false)
    }

    private fun setPlayingState(id: Int, playing: Boolean) {
        _playingSampleIds.update { if (playing) it + id else it - id }
    }

    override fun cleanup() {
        audioPlayer.cleanup()
    }

    private fun getDefaultSampleColors() = listOf("#008B8B", "#F50057", "#00C853", "#D500F9")
    private fun generateDefaultSamples() = (0..3).map { i ->
        SampleEntity(
            i,
            "S${i + 1}",
            80,
            false,
            getDefaultSampleColors()[i],
            null,
            null
        )
    }

    // --- Audio Library Repository Implementation ---

    override fun observeLibrary() =
        audioLibraryDao.observeAllAudio().map { it.map { e -> e.toDomain() } }

    override suspend fun getLibraryItems() = audioLibraryDao.getAllAudio().map { it.toDomain() }

    override suspend fun deleteAudioFile(item: AudioLibraryItem) {
        val f = File(context.filesDir, item.filePath)
        if (f.exists()) f.delete()
        audioLibraryDao.deleteAudio(item.toEntity())
    }

    override fun getAudioFile(item: AudioLibraryItem) = File(context.filesDir, item.filePath)

    override suspend fun searchLibrary(query: String) =
        audioLibraryDao.searchAudio(query).map { it.toDomain() }

    override suspend fun addAudioFile(sourceUri: Uri, originalName: String): AudioLibraryItem =
        withContext(Dispatchers.IO) {
            val fileName = "lib_${System.currentTimeMillis()}_$originalName"
            val destFile = File(context.filesDir, fileName)
            context.contentResolver.openInputStream(sourceUri)
                ?.use { input -> FileOutputStream(destFile).use { input.copyTo(it) } }

            var duration = 0L
            try {
                val ret = MediaMetadataRetriever()
                ret.setDataSource(destFile.absolutePath)
                duration = ret.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
                ret.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val entity = AudioLibraryEntity(
                originalName,
                fileName,
                destFile.length(),
                duration,
                System.currentTimeMillis()
            )
            audioLibraryDao.insertAudio(entity)
            return@withContext entity.toDomain()
        }

    private fun SampleEntity.toDomain() =
        SamplePad(id, name, volume, loop, color, audioFileName, sourceName)

    private fun SamplePad.toEntity() =
        SampleEntity(id, name, volume, loop, color, audioFileName, sourceName)

    private fun AudioLibraryEntity.toDomain() =
        AudioLibraryItem(name, filePath, sizeBytes, durationMs, addedTimestamp)

    private fun AudioLibraryItem.toEntity() =
        AudioLibraryEntity(name, filePath, sizeBytes, durationMs, addedTimestamp)
}














