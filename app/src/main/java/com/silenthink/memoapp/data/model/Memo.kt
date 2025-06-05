package com.silenthink.memoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "memos")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdDate: Date,
    val modifiedDate: Date,
    val imagePath: String? = null,
    val category: String = "默认",
    val priority: Int = 0
) 