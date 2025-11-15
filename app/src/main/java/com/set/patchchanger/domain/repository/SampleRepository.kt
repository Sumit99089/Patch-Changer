package com.set.patchchanger.domain.repository


import android.net.Uri
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.SamplePad
import kotlinx.coroutines.flow.Flow

interface SampleRepository {
    fun observeSamples(): Flow<List<SamplePad>>
    suspend fun getSamples(): List<SamplePad>
    suspend fun updateSample(sample: SamplePad)
    suspend fun clearSampleAudio(sampleId: Int)
    suspend fun saveSampleAudioFromUri(sampleId: Int, sourceUri: Uri, originalName: String): String
    suspend fun saveSampleAudioFromLibrary(sampleId: Int, libraryItem: AudioLibraryItem): String
    suspend fun resetSamples()
    suspend fun triggerSampleAudio(sampleId: Int)
    fun stopSample(sampleId: Int)
    fun cleanup()
}