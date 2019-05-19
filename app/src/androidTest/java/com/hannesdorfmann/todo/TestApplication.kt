package com.hannesdorfmann.todo

import com.hannesdorfmann.todo.create.CreateTodoItemFragment
import com.hannesdorfmann.todo.di.ApplicationModule
import com.hannesdorfmann.todo.di.viewBinderFactory
import com.hannesdorfmann.todo.domain.IdGenerator
import com.hannesdorfmann.todo.domain.TodoRepository
import com.hannesdorfmann.todo.list.TodoListViewFragment

class TestApplication : TodoApplication() {

    override fun applicatinoModule(): ApplicationModule = object : ApplicationModule(
        context = this,
        viewBinderFactory = viewBinderFactory {
            TodoListViewFragment::class bindTo { RecordingTodoListViewBinder(it) }
            CreateTodoItemFragment::class bindTo { RecordingCreateTodoItemViewBinder(it) }
        }) {

        override fun provideTodoRepository(): TodoRepository = InMemoryTodoRepository()

        override fun providesIdGenerator(): IdGenerator = IncrementIdGenerator()
    }
}