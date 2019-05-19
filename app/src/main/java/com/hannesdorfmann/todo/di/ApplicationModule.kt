package com.hannesdorfmann.todo.di

import android.content.Context
import androidx.room.Room
import com.hannesdorfmann.todo.domain.IdGenerator
import com.hannesdorfmann.todo.domain.TodoRepository
import com.hannesdorfmann.todo.domain.db.TodoDatabase
import dagger.Module
import dagger.Provides
import java.util.UUID
import javax.inject.Singleton

@Module
open class ApplicationModule(
    private val context: Context,
    private val viewBinderFactory: ViewBinderFactory
) {

    @Provides
    @Singleton
    open fun provideTodoRepository(): TodoRepository {
        val todoDatabase = Room.databaseBuilder(context, TodoDatabase::class.java, "TodoDatabase")
            .build()

        return todoDatabase.dao()
    }

    @Provides
    @Singleton
    open fun provideViewBinderFactory(): ViewBinderFactory = viewBinderFactory

    @Provides
    @Singleton
    open fun providesIdGenerator(): IdGenerator = object : IdGenerator {
        override fun nextId(): String = UUID.randomUUID().toString()
    }
}