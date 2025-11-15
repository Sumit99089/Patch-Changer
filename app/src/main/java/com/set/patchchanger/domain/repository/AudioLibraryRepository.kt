package com.set.patchchanger.domain.repository


import android.net.Uri
import com.set.patchchanger.domain.model.AudioLibraryItem
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioLibraryRepository {
    fun observeLibrary(): Flow<List<AudioLibraryItem>>
    suspend fun getLibraryItems(): List<AudioLibraryItem>
    suspend fun addAudioFile(sourceUri: Uri, originalName: String): AudioLibraryItem
    suspend fun deleteAudioFile(item: AudioLibraryItem)
    fun getAudioFile(item: AudioLibraryItem): File
    suspend fun searchLibrary(query: String): List<AudioLibraryItem>
}