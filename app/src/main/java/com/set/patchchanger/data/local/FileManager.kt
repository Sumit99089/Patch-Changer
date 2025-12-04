package com.set.patchchanger.data.local

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class FileManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val folderName = "PatchChanger"

    // Save JSON to Documents/PatchChanger/ via MediaStore (Works on Android 10/11/12+)
    fun saveJsonToDocuments(fileName: String, jsonContent: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/$folderName")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)

                uri?.let {
                    resolver.openOutputStream(it)?.use { stream ->
                        stream.write(jsonContent.toByteArray())
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, values, null, null)
                    true
                } ?: false
            } else {
                // Legacy approach for Android 9 and below
                val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val appDir = File(docsDir, folderName)
                if (!appDir.exists()) appDir.mkdirs()
                val file = File(appDir, fileName)
                file.writeText(jsonContent)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads content from a File object.
     * NOTE: This is only used for files in internal app storage (cache/filesDir).
     * Do not use for external storage files on Android 11+.
     */
    fun readFileContent(file: File): String {
        return file.readText()
    }
}