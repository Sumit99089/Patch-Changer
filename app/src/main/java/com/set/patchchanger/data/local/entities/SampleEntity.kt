package com.set.patchchanger.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "samples")
data class SampleEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "volume") val volume: Int,
    @ColumnInfo(name = "loop") val loop: Boolean,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "audio_file_name") val audioFileName: String?,
    @ColumnInfo(name = "source_name") val sourceName: String?
)