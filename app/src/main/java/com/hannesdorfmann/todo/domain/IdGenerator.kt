package com.hannesdorfmann.todo.domain

interface IdGenerator {
    fun nextId() : String
}