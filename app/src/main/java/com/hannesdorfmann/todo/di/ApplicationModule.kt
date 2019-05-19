package com.hannesdorfmann.todo.di

import android.content.Context
import androidx.room.Room
import com.hannesdorfmann.todo.domain.TodoRepository
import com.hannesdorfmann.todo.domain.db.TodoDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(
    private val context: Context,
    private val viewBinderFactory: ViewBinderFactory
) {

    @Provides
    @Singleton
    fun provideTodoRepository(): TodoRepository {
        val todoDatabase = Room.databaseBuilder(context, TodoDatabase::class.java, "TodoDatabase")
            .build()

        return todoDatabase.dao()
    }

    @Provides
    @Singleton
    fun provideViewBinderFactory(): ViewBinderFactory = viewBinderFactory
}