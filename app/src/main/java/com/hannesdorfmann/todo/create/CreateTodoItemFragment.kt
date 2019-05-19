package com.hannesdorfmann.todo.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.hannesdorfmann.todo.R
import com.hannesdorfmann.todo.ext.daggerComponent
import com.hannesdorfmann.todo.ext.getViewBinderFromDagger
import com.hannesdorfmann.todo.ext.lazyViewModel
import javax.inject.Inject
import javax.inject.Provider

class CreateTodoItemFragment : Fragment() {

    @Inject
    lateinit var provider: Provider<CreateWizardViewModel>

    private val viewModel by lazyViewModel { provider }
    private val viewBinder: CreateTodoItemViewBinder by getViewBinderFromDagger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        daggerComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_create, container, false)

    override fun onStart() {
        super.onStart()
        viewBinder.actionListener = viewModel::input
        viewModel.state.observe(this, Observer {
            viewBinder.render(it)
        })
    }
}