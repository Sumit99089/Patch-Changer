package com.set.patchchanger.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val index: Int,
    @ColumnInfo(name = "name") val name: String
)
