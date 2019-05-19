package com.hannesdorfmann.todo

import android.app.Application
import com.hannesdorfmann.todo.create.CreateTodoItemFragment
import com.hannesdorfmann.todo.create.CreateTodoItemViewBinder
import com.hannesdorfmann.todo.di.ApplicationModule
import com.hannesdorfmann.todo.di.DaggerApplicationComponent
import com.hannesdorfmann.todo.di.viewBinderFactory
import com.hannesdorfmann.todo.list.TodoListViewBinder
import com.hannesdorfmann.todo.list.TodoListViewFragment

open class TodoApplication : Application() {
    val component by lazy {
        DaggerApplicationComponent.builder()
            .applicationModule(applicatinoModule())
            .build()
    }

    open protected fun applicatinoModule(): ApplicationModule = ApplicationModule(
        context = this,
        viewBinderFactory = viewBinderFactory {
            TodoListViewFragment::class bindTo { TodoListViewBinder(it) }
            CreateTodoItemFragment::class bindTo { CreateTodoItemViewBinder(it) }
        })
}