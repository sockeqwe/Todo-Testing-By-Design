package com.hannesdorfmann.todo

import android.view.View
import com.hannesdorfmann.todo.list.TodoListStateMachine
import com.hannesdorfmann.todo.list.TodoListViewBinder

class ListenableTodoListViewBinder(root: View) : TodoListViewBinder(root) {
    companion object {
        // I'm just to lazy to setup dagger properly ... dont do that at home
        lateinit var stateChangeListener: (TodoListStateMachine.State) -> Unit
    }

    override fun render(state: TodoListStateMachine.State) {
        super.render(state)
        stateChangeListener(state)
    }
}