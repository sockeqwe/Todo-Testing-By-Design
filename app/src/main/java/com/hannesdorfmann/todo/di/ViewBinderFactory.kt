package com.hannesdorfmann.todo.di

import android.view.View
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

typealias ViewBinderInstantiator = (rootView: View) -> Any?

/**
 * In a real world application you would use AssistedInject instead of this.
 * We are just to lazy to setup assisted inject properly.
 */
class ViewBinderFactory {

    private val instantiatorMap = HashMap<KClass<out Fragment>, ViewBinderInstantiator>()

    fun get(fragment: Fragment, rootView: View): Any? {
        val instantiator = instantiatorMap[fragment.javaClass.kotlin]
        return instantiator?.invoke(rootView)
    }

    infix fun KClass<out Fragment>.bindTo(instantiator: ViewBinderInstantiator) {
        instantiatorMap[this] = instantiator
    }
}

fun viewBinderFactory(block: ViewBinderFactory.() -> Unit): ViewBinderFactory {
    val factory = ViewBinderFactory()
    block(factory)
    return factory
}

