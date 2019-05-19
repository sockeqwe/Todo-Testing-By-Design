package com.hannesdorfmann.todo.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TodoListViewModel @Inject constructor(stateMachine: TodoListStateMachine) : ViewModel() {

    private val disposable: CompositeDisposable = CompositeDisposable()
    private val inputRelay = PublishRelay.create<TodoListStateMachine.Action>()

    val state = MutableLiveData<TodoListStateMachine.State>()

    init {
        disposable.addAll(

            stateMachine.state
                .subscribeOn(Schedulers.io())
                .subscribe { state.postValue(it) },

            inputRelay
                .observeOn(Schedulers.io())
                .subscribe(stateMachine::input)
        )
    }

    fun input(action: TodoListStateMachine.Action) {
        inputRelay.accept(action)
    }

    override fun onCleared() {
        disposable.dispose()
    }
}