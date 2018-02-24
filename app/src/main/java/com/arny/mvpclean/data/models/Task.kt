
package com.arny.mvpclean.data.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

import java.util.UUID

@Entity(tableName = "Tasks")
data class Task(@ColumnInfo(name = "title")
                val title: String?,
                @ColumnInfo(name = "description")
                val description: String?,
                @PrimaryKey
                @ColumnInfo(name = "entryid")
                val id: String,
                @ColumnInfo(name = "completed")
                val isCompleted: Boolean) {

    fun titleForList(): String? = if (!title.isNullOrEmpty()) {
        title
    } else {
        description
    }

    fun isActive(): Boolean = !isCompleted

    fun isEmpty(): Boolean = title.isNullOrEmpty() && description.isNullOrEmpty()

    @Ignore
    constructor(title: String?, description: String?) : this(title, description, UUID.randomUUID().toString(), false)

    @Ignore
    constructor(title: String?, description: String?, id: String) : this(title, description, id, false)

    @Ignore
    constructor(title: String?, description: String?, completed: Boolean) : this(title, description, UUID.randomUUID().toString(), completed)
}
