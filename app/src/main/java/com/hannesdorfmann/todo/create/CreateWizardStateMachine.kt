package com.hannesdorfmann.todo.create

import android.util.Log
import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.hannesdorfmann.todo.domain.TodoItem
import com.hannesdorfmann.todo.domain.TodoRepository
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.UUID
import javax.inject.Inject

sealed class State {
    data class EnterTextState(val title: String) : State()
    data class SummaryState(val title: String, val showError: Boolean) : State()
    data class SavingInProgressSate(val title: String) : State()
    data class SuccessfulSavedState(val title: String) : State()
}

sealed class Action {
    /**
     * Go to the next step in the wizard
     */
    object NextStepAction : Action()

    /**
     * Go to the previous step in the wizard
     */
    object PreviousSteAction : Action()

    data class TitleChangedAction(val title: String) : Action()
}

private object ErrorWhileSavingAction : Action()
private object SavedSuccessfullyAction : Action()

// Todo save the current state into a bundle to survive process death (out of scope for this demo)
// and start with the restored state from bundle as initial state of the state machine
class CreateWizardStateMachine @Inject constructor(private val repository: TodoRepository) {

    val input = PublishRelay.create<Action>()

    val state: Observable<State> =
        input.reduxStore<State, Action>(
            State.EnterTextState(""),
            listOf(::saveSideEffect)
        ) { state: State, action: Action ->
            log("reducer: $state --- $action")
            when (state) {
                is State.EnterTextState -> when (action) {
                    is Action.TitleChangedAction -> State.EnterTextState(action.title)
                    is Action.NextStepAction -> State.SummaryState(state.title, showError = false)
                    else -> throw IllegalStateException(
                        "Action of type $action is not allowed inside $state"
                    )
                }

                is State.SummaryState -> when (action) {
                    is Action.NextStepAction -> State.SavingInProgressSate(state.title) // Triggers side effect
                    is Action.PreviousSteAction -> State.EnterTextState(state.title)
                    else -> throw IllegalStateException(
                        "Action of type $action is not allowed inside $state"
                    )
                }

                is State.SavingInProgressSate -> when (action) {
                    is SavedSuccessfullyAction -> State.SuccessfulSavedState(state.title)
                    is ErrorWhileSavingAction -> State.SummaryState(state.title, showError = true)
                    else -> throw IllegalStateException(
                        "Action of type $action is not allowed inside $state"
                    )
                }
                is State.SuccessfulSavedState -> throw IllegalStateException(
                    "Action of type $action is not allowed inside $state. SuccessfulSavedState is the final state, " +
                        "no more actions allowed after having reached this state"
                )
            }
        }

    private fun saveSideEffect(actions: Observable<Action>, stateAccessor: StateAccessor<State>): Observable<Action> =
        actions
            .ofType(Action.NextStepAction::class.java)
            .switchMap {
                val state = stateAccessor()
                log("saveSideEffect $state $it")
                if (state is State.SavingInProgressSate) {
                    Observable.fromCallable {
                        repository.add(
                            TodoItem(
                                id = UUID.randomUUID().toString(),
                                done = false,
                                imagePath = null,
                                text = state.title
                            )
                        )
                        SavedSuccessfullyAction as Action
                    }
                        .subscribeOn(Schedulers.io())
                        .onErrorReturn { ErrorWhileSavingAction }
                } else {
                    Observable.empty()
                }
            }

    private fun log(msg: String) {
        Log.d("CreateWizardSM", msg)
    }
}