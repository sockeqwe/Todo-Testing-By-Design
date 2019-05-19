package com.hannesdorfmann.todo.domain.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hannesdorfmann.todo.domain.TodoItem
import com.hannesdorfmann.todo.domain.TodoRepository
import io.reactivex.Observable

@Dao
abstract class RoomTodoRepository : TodoRepository {

    @Query("SELECT * FROM ${TodoItem.TABLE_NAME}")
    abstract override fun getAll(): Observable<List<TodoItem>>

    @Insert
    abstract override fun add(item: TodoItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract override fun update(item: TodoItem)

    @Delete
    abstract override fun remove(item: TodoItem)
}