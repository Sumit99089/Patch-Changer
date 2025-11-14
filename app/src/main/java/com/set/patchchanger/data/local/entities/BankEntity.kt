package com.set.patchchanger.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banks")
data class BankEntity(
    @PrimaryKey val index: Int,
    @ColumnInfo(name = "name") val name: String
)
