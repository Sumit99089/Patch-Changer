package com.set.patchchanger.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.SoundPool
import android.net.Uri
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    @param:ApplicationContext private val context: Context,
    private val scope: CoroutineScope
) : SampleRepository, AudioLibraryRepository {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(16)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSoundIds = mutableMapOf<String, Int>()
    private val activeStreamIds = mutableMapOf<Int, Int>()
    private val activeStreamVolumes = mutableMapOf<Int, Float>()
    private val sampleDurations = mutableMapOf<String, Long>()
    private val playbackJobs = mutableMapOf<Int, Job>()

    private val _playingSampleIds = MutableStateFlow<Set<Int>>(emptySet())

    override fun observePlayingStates(): Flow<Set<Int>> = _playingSampleIds.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            sampleDao.observeAllSamples().collect { entities ->
                if (entities.isEmpty()) {
                    sampleDao.insertSamples(generateDefaultSamples())
                } else {
                    entities.forEach { entity ->
                        entity.audioFileName?.let { fileName ->
                            val file = File(context.filesDir, fileName)
                            if (file.exists()) loadSound(file.absolutePath)
                        }
                    }
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
        sample.audioFileName?.let { name ->
            val file = File(context.filesDir, name)
            if (file.exists()) loadSound(file.absolutePath)
        }
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
        if (sourceFile.exists()) {
            sourceFile.copyTo(destFile, overwrite = true)
            updateSampleDb(sampleId, fileName, libraryItem.name)
            loadSound(destFile.absolutePath)
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
        activeStreamIds.keys.toList().forEach { stopSample(it) }
        sampleDao.deleteAll()
        sampleDao.insertSamples(generateDefaultSamples())
        loadedSoundIds.values.forEach { soundPool.unload(it) }
        loadedSoundIds.clear()
        sampleDurations.clear()
    }

    override suspend fun triggerSampleAudio(sampleId: Int) {
        val sample = sampleDao.getSampleById(sampleId)?.toDomain() ?: return
        if (sample.audioFileName == null) return

        // If currently playing, stop it (Toggle behavior)
        if (_playingSampleIds.value.contains(sampleId)) {
            stopSample(sampleId)
            return
        }

        val fullPath = File(context.filesDir, sample.audioFileName).absolutePath

        // 1. Ensure sound is loaded
        if (!loadedSoundIds.containsKey(fullPath)) {
            // Load synchronously (id generation) if missed by init
            val id = soundPool.load(fullPath, 1)
            loadedSoundIds[fullPath] = id
        }
        val soundId = loadedSoundIds[fullPath] ?: return

        // 2. Ensure Duration is known (Crucial for blinking duration)
        if (!sampleDurations.containsKey(fullPath) || sampleDurations[fullPath] == 0L) {
            withContext(Dispatchers.IO) {
                try {
                    val ret = MediaMetadataRetriever()
                    ret.setDataSource(fullPath)
                    val dur = ret.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L
                    sampleDurations[fullPath] = dur
                    ret.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val vol = sample.volume / 100f
        val streamId = soundPool.play(soundId, vol, vol, 1, if (sample.loop) -1 else 0, 1f)

        if (streamId != 0) {
            activeStreamIds[sampleId] = streamId
            activeStreamVolumes[sampleId] = vol
            setPlayingState(sampleId, true)

            // Cancel any old job for this ID
            playbackJobs[sampleId]?.cancel()

            if (!sample.loop) {
                // Use the fetched duration, default to 1s only if extraction completely failed
                val duration = sampleDurations[fullPath]?.takeIf { it > 0 } ?: 1000L

                playbackJobs[sampleId] = scope.launch {
                    delay(duration)
                    // Check if we are still playing the same stream (user didn't stop and start again)
                    if (activeStreamIds[sampleId] == streamId) {
                        setPlayingState(sampleId, false)
                        activeStreamIds.remove(sampleId)
                    }
                }
            }
        }
    }

    override fun stopSample(sampleId: Int) {
        val streamId = activeStreamIds[sampleId] ?: return

        // Cancel the auto-stop timer immediately so it doesn't fire later
        playbackJobs[sampleId]?.cancel()

        scope.launch {
            val startVol = activeStreamVolumes[sampleId] ?: 1.0f
            val steps = 10
            val delayPerStep = 5L

            for (i in 1..steps) {
                val newVol = startVol * (1.0f - (i.toFloat() / steps))
                soundPool.setVolume(streamId, newVol, newVol)
                delay(delayPerStep)
            }

            soundPool.stop(streamId)
            activeStreamIds.remove(sampleId)
            activeStreamVolumes.remove(sampleId)
            setPlayingState(sampleId, false)
        }
    }

    private fun setPlayingState(id: Int, playing: Boolean) {
        _playingSampleIds.update { if (playing) it + id else it - id }
    }

    private fun loadSound(path: String) {
        if (!loadedSoundIds.containsKey(path)) {
            val id = soundPool.load(path, 1)
            loadedSoundIds[path] = id
            // Metadata extraction should be done in background if called from UI,
            // but init calls this from Dispatchers.IO, so it's safe.
            try {
                val ret = MediaMetadataRetriever()
                ret.setDataSource(path)
                sampleDurations[path] =
                    ret.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L
                ret.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun cleanup() {
        soundPool.release()
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