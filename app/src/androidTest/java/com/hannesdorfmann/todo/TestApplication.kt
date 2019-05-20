package com.hannesdorfmann.todo

import com.hannesdorfmann.todo.create.CreateTodoItemFragment
import com.hannesdorfmann.todo.di.ApplicationComponent
import com.hannesdorfmann.todo.di.ApplicationModule
import com.hannesdorfmann.todo.di.DaggerApplicationComponent
import com.hannesdorfmann.todo.di.viewBinderFactory
import com.hannesdorfmann.todo.domain.IdGenerator
import com.hannesdorfmann.todo.domain.TodoRepository
import com.hannesdorfmann.todo.list.TodoListViewFragment

class TestApplication : TodoApplication() {

    companion object {
        // I'm just to lazy to setup dagger properly, Please don't do this at home!
        val todoRepository by lazy { InMemoryTodoRepository() }
        val idGenerator by lazy { IncrementIdGenerator() }
    }

    private val applicationModule: ApplicationModule = object : ApplicationModule(
        context = this,
        viewBinderFactory = viewBinderFactory {
            TodoListViewFragment::class bindTo { ListenableTodoListViewBinder(it) }
            CreateTodoItemFragment::class bindTo { ListenableCreateTodoItemViewBinder(it) }
        }) {

        override fun provideTodoRepository(): TodoRepository = todoRepository

        override fun providesIdGenerator(): IdGenerator = idGenerator
    }

    override fun buildApplicationComponent(): ApplicationComponent = DaggerApplicationComponent.builder()
        .applicationModule(applicationModule)
        .build()

}