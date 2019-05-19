package com.hannesdorfmann.todo

import com.hannesdorfmann.todo.domain.IdGenerator

class IncrementIdGenerator : IdGenerator {

    private var counter: Int = 0

    @Synchronized
    override fun nextId(): String {
        counter++
        return counter.toString()
    }
}