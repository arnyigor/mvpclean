package com.arny.mvpclean.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "folders")
data class CleanFolder(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long? = null) {
    @Ignore
    var title: String? = null
    @ColumnInfo(name = "path")
    var path: String? = null
    @Ignore
    var size: Long = 0.toLong()

    @Ignore
    constructor() : this(id = null)

    @Ignore
    constructor(path: String? = null) : this(id = null) {
        this.path = path
    }
}
