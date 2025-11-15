package com.set.patchchanger.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioLibraryItem(
    val name: String,
    val filePath: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val addedTimestamp: Long
) : Parcelable