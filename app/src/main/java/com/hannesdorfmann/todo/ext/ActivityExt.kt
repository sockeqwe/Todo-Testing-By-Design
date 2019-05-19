package com.hannesdorfmann.todo.ext

import android.app.Activity
import androidx.fragment.app.Fragment
import com.hannesdorfmann.todo.TodoApplication
import com.hannesdorfmann.todo.di.ApplicationComponent

/**
 * Convinient way to access daggers application component
 */
val Activity.daggerComponent: ApplicationComponent
    get() = (application as TodoApplication).component