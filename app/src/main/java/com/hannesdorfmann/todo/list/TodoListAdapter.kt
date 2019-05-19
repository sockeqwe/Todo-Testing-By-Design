package com.hannesdorfmann.todo.list

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.todo.R
import com.hannesdorfmann.todo.domain.TodoItem

class TodoListAdapter(
    private val inflater: LayoutInflater,
    private val actionHandler: (TodoListStateMachine.Action) -> Unit
) : RecyclerView.Adapter<TodoListAdapter.TodoViewHolder>() {

    var items = emptyList<TodoItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder =
        TodoViewHolder(inflater.inflate(R.layout.item_todo, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class TodoViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val textView: TextView = v.findViewById(R.id.text)
        private val checkBox: CheckBox = v.findViewById(R.id.checkbox)

        private lateinit var currentItem: TodoItem

        init {
            checkBox.setOnClickListener {
                actionHandler(TodoListStateMachine.Action.ToggleCheckedTodoItemAction(currentItem))
            }
        }

        fun bind(item: TodoItem) {
            currentItem = item

            checkBox.isChecked = item.done
            if (item.done) {
                val ssBuilder = SpannableStringBuilder(item.text)
                ssBuilder.setSpan(StrikethroughSpan(), 0, item.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                textView.text = ssBuilder
            } else {
                textView.text = item.text
            }
        }
    }
}