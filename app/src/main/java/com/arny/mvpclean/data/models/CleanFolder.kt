package com.arny.mvpclean.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import java.util.UUID

@Entity(tableName = "folders")
data class CleanFolder(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long? = null) {
    var title: String? = null
    var path: String? = null
    var size: Long = 0.toLong()

    @Ignore
    constructor() : this(null)
}
