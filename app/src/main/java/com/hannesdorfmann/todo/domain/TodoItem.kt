package com.hannesdorfmann.todo.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = TodoItem.TABLE_NAME)
data class TodoItem(
    @PrimaryKey @ColumnInfo val id: String,
    @ColumnInfo val text: String,
    @ColumnInfo val done: Boolean
) {
    companion object {
        const val TABLE_NAME = "TodoItems"
    }
}