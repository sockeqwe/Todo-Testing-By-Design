package com.hannesdorfmann.todo.di

import com.hannesdorfmann.todo.create.CreateTodoItemFragment
import com.hannesdorfmann.todo.list.TodoListViewFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = [ApplicationModule::class])
@Singleton
interface ApplicationComponent {

    fun inject(f: TodoListViewFragment)
    fun inject(f: CreateTodoItemFragment)

    fun viewBinderFactory() : ViewBinderFactory
}