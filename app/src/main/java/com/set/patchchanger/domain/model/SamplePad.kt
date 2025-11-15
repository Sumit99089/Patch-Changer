package com.set.patchchanger.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SamplePad(
    val id: Int,
    val name: String,
    val volume: Int,
    val loop: Boolean,
    val color: String,
    val audioFileName: String?,
    val sourceName: String?
) : Parcelable {
    fun hasAudio(): Boolean = !audioFileName.isNullOrEmpty()
}