package com.set.patchchanger.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class FileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Writes text data to the specified URI using the ContentResolver (SAF).
     */
    suspend fun writeTextToUri(uri: Uri, text: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(text.toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Reads text content from the specified URI using the ContentResolver (SAF).
     */
    suspend fun readTextFromUri(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).readText()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Resolves the display name of a file from its URI.
     */
    suspend fun getFileNameFromUri(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            var name = "unknown"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) name = cursor.getString(index)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            name
        }
    }
}