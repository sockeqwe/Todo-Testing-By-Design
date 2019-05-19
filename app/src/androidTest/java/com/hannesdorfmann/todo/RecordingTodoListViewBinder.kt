package com.hannesdorfmann.todo

import android.view.View
import com.hannesdorfmann.todo.list.TodoListStateMachine
import com.hannesdorfmann.todo.list.TodoListViewBinder
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable

class RecordingTodoListViewBinder(root: View) : TodoListViewBinder(root) {
    companion object {
        lateinit var INSTANCE: RecordingTodoListViewBinder
    }

    init {
        INSTANCE = this // I'm just to lazy to setup dagger properly
    }

    private val _subject = ReplayRelay.create<TodoListStateMachine.State>()
    val states: Observable<TodoListStateMachine.State> = _subject

    override fun render(state: TodoListStateMachine.State) {
        super.render(state)
        _subject.accept(state)
    }
}