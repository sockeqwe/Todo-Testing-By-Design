package com.hannesdorfmann.todo.domain.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hannesdorfmann.todo.domain.TodoItem

@Database(entities = [TodoItem::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun dao(): RoomTodoRepository
}