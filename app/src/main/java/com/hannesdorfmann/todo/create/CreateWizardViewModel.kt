package com.hannesdorfmann.todo.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class CreateWizardViewModel @Inject constructor(private val stateMachine: CreateWizardStateMachine) : ViewModel() {

    val state = MutableLiveData<State>()

    private val disposable = stateMachine.state.subscribe { state.postValue(it) }

    fun input(action: Action) {
        stateMachine.input.accept(action)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}