package com.hannesdorfmann.todo

import com.hannesdorfmann.todo.domain.TodoItem
import com.hannesdorfmann.todo.domain.TodoRepository
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.Collections

class InMemoryTodoRepository(private val backgroundScheduler: Scheduler = Schedulers.io()) : TodoRepository {

    private val db: MutableMap<String, TodoItem> = Collections.synchronizedMap(LinkedHashMap())
    private val notifier = PublishRelay.create<Unit>()

    override fun getAll(): Observable<List<TodoItem>> =
        notifier
            .observeOn(backgroundScheduler)
            .startWith(Unit)
            .map { db.values.toList() }

    override fun add(item: TodoItem) {
        db[item.id] = item
        Thread.sleep(100) // Simulate some io delay
        notifier.accept(Unit)
    }

    override fun update(item: TodoItem) {
        db[item.id] = item
        Thread.sleep(100) // Simulate some io delay
        notifier.accept(Unit)
    }

    override fun remove(item: TodoItem) {
        db.remove(item.id)
        Thread.sleep(100) // Simulate some io delay
        notifier.accept(Unit)
    }

    fun clear() {
        db.clear()
    }
}