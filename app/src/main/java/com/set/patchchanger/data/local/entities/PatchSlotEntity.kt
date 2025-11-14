package com.set.patchchanger.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity for patch slots.
 *
 * Room is Android's SQL database abstraction layer.
 * It provides compile-time verification of SQL queries and
 * automatic conversion between Kotlin objects and database rows.
 *
 * @Entity annotation marks this as a database table
 * @PrimaryKey marks the primary key column
 */

@Entity(tableName = "patch_slots")
data class PatchSlotEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "selected") val selected: Boolean,
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "msb") val msb: Int,
    @ColumnInfo(name = "lsb") val lsb: Int,
    @ColumnInfo(name = "pc") val pc: Int,
    @ColumnInfo(name = "volume") val volume: Int,
    @ColumnInfo(name = "performance_name") val performanceName: String,
    @ColumnInfo(name = "display_name_type") val displayNameType: String,
    @ColumnInfo(name = "assigned_sample") val assignedSample: Int
)