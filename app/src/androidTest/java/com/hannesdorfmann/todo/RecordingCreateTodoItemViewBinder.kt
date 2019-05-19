package com.hannesdorfmann.todo

import android.view.View
import com.hannesdorfmann.todo.create.CreateTodoItemViewBinder
import com.hannesdorfmann.todo.create.State
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable

class RecordingCreateTodoItemViewBinder(root: View) : CreateTodoItemViewBinder(root) {
    companion object {
        lateinit var INSTANCE: RecordingCreateTodoItemViewBinder
    }

    init {
        INSTANCE = this // I'm just to lazy to setup dagger properly
    }

    private val _subject = ReplayRelay.create<State>()
    val states: Observable<State> = _subject

    override fun render(state: State) {
        super.render(state)
        _subject.accept(state)
    }
}