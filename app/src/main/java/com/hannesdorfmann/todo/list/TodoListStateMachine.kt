package com.hannesdorfmann.todo.list

import com.hannesdorfmann.todo.domain.TodoItem
import com.hannesdorfmann.todo.domain.TodoRepository
import io.reactivex.Observable
import javax.inject.Inject

class TodoListStateMachine @Inject constructor(private val repository: TodoRepository) {

    sealed class State {
        object Loading : State()
        data class Content(val items: List<TodoItem>) : State()
        object Error : State()
    }

    sealed class Action {
        data class ToggleCheckedTodoItemAction(val item: TodoItem) : Action()
        data class DeleteTodoItemAction(val item: TodoItem) : Action()
    }

    val state: Observable<State> =
        repository.getAll()
            .map { State.Content(it) as State }
            .onErrorReturn { State.Error }
            .startWith(State.Loading)

    fun input(action: Action) {
        when (action) {
            is Action.ToggleCheckedTodoItemAction -> repository.update(action.item.copy(done = !action.item.done))
            is Action.DeleteTodoItemAction -> repository.remove(action.item)
        }
    }
}