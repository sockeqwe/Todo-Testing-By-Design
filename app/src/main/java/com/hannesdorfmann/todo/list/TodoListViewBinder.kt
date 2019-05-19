package com.hannesdorfmann.todo.list

import android.view.LayoutInflater
import android.view.View
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.todo.R
import com.hannesdorfmann.todo.ext.gone
import com.hannesdorfmann.todo.ext.visible

open class TodoListViewBinder(root: View) {

    lateinit var actionListener: (TodoListStateMachine.Action) -> Unit

    private val addNewItem = root.findViewById<View>(R.id.newItem)
    private val adapter = TodoListAdapter(LayoutInflater.from(root.context), { actionListener(it) })
    private val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
    private val error = root.findViewById<View>(R.id.error)
    private val loading = root.findViewById<View>(R.id.loading)

    init {
        recyclerView.adapter = adapter
        addNewItem.setOnClickListener { Navigation.findNavController(root).navigate(R.id.action_show_create_new_item) }
    }

    open fun render(state: TodoListStateMachine.State) = when (state) {
        TodoListStateMachine.State.Loading -> {
            loading.visible()
            error.gone()
            recyclerView.gone()
            addNewItem.gone()
        }
        TodoListStateMachine.State.Error -> {
            loading.gone()
            error.visible()
            recyclerView.gone()
            addNewItem.gone()
        }
        is TodoListStateMachine.State.Content -> {
            loading.gone()
            error.gone()
            recyclerView.visible()
            addNewItem.visible()

            adapter.items = state.items
            adapter.notifyDataSetChanged()
        }
    }
}