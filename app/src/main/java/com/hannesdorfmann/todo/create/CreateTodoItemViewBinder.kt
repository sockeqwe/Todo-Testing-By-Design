package com.hannesdorfmann.todo.create

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.transition.TransitionManager
import com.hannesdorfmann.todo.R
import com.hannesdorfmann.todo.ext.gone
import com.hannesdorfmann.todo.ext.visible

open class CreateTodoItemViewBinder(private val rootView: View) {

    private val step1 = rootView.findViewById<View>(R.id.create_step1)
    private val step2 = rootView.findViewById<View>(R.id.create_step2)
    private val step1Title = rootView.findViewById<EditText>(R.id.step1Title)
    private val step1NextButton = step1.findViewById<View>(R.id.button)
    private val step2CreateButton = step2.findViewById<View>(R.id.create)
    private val step2BackButton = step2.findViewById<View>(R.id.previous)
    private val step2Loading = step2.findViewById<View>(R.id.loading)
    private val step2ErrorMessage = step2.findViewById<View>(R.id.errorMessage)
    private val step2SummaryTitle = step2.findViewById<TextView>(R.id.titleSummary)
    private val step2Summary = step2.findViewById<View>(R.id.summary)

    init {
        step1NextButton.setOnClickListener {
            hideSoftInputFromWindow()
            actionListener(Action.NextStepAction)
        }
        step2CreateButton.setOnClickListener {
            actionListener(Action.NextStepAction)
        }
        step2BackButton.setOnClickListener {
            actionListener(Action.PreviousSteAction)
        }
        step1Title.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null)
                    actionListener(Action.TitleChangedAction(s.toString()))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    lateinit var actionListener: (Action) -> Unit

    open fun render(state: State) {
        TransitionManager.beginDelayedTransition(rootView as ViewGroup)
        when (state) {
            is State.EnterTextState -> {
                if (state.title != step1Title.text.toString())
                    step1Title.setText(state.title)

                step2.gone()
                step1.visible()
            }
            is State.SummaryState -> {
                step1.gone()
                step2.visible()
                step2Loading.gone()

                if (state.showError)
                    step2ErrorMessage.visible()
                else
                    step2ErrorMessage.gone()

                step2CreateButton.visible()
                step2BackButton.visible()
                step2Summary.visible()
                step2SummaryTitle.visible()
                step2SummaryTitle.setText(state.title)
            }
            is State.SavingInProgressSate -> {
                step1.gone()
                step2.visible()
                step2Loading.visible()
                step2ErrorMessage.gone()
                step2Summary.gone()
                step2SummaryTitle.gone()
                step2CreateButton.gone()
                step2BackButton.gone()
            }
            is State.SuccessfulSavedState -> {
                Navigation.findNavController(rootView).popBackStack()
            }

        }
    }

    private fun hideSoftInputFromWindow() {
        val imm = rootView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(step1Title.windowToken, 0)
    }
}