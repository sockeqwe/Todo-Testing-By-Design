package com.hannesdorfmann.todo.domain

import io.reactivex.Observable

interface TodoRepository {
    fun getAll() : Observable<List<TodoItem>>
    fun add(item : TodoItem)
    fun update(item : TodoItem)
    fun remove(item : TodoItem)
}