package com.hannesdorfmann.todo.ext

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.hannesdorfmann.todo.TodoApplication
import com.hannesdorfmann.todo.di.ApplicationComponent
import com.hannesdorfmann.todo.di.ViewBinderFactory
import javax.inject.Provider
import kotlin.reflect.KProperty

/**
 * A very simple [ViewModelProvider.Factory] that uses [Provider].
 * This is useful in combination with Dagger.
 */
class SimpleViewModelProviderFactory<T : ViewModel>(
    private val provider: Provider<T>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = provider.get() as T
}

/**
 * Creates a ViewModel lazily.
 * @param T The type of the ViewModel
 */
inline fun <reified T : ViewModel> FragmentActivity.lazyViewModel(
    noinline providerInitializer: () -> Provider<T>
): LazyViewModelProvider<T> {
    val actitiy = this
    return LazyViewModelProvider(
        providerInitializer = providerInitializer,
        viewModelRetriever = { provider: Provider<T> ->
            val factory = SimpleViewModelProviderFactory<T>(provider)
            ViewModelProviders.of(actitiy, factory).get(T::class.java)
        }
    )
}

/**
 * Creates a ViewModel lazily.
 * @param T The type of the ViewModel
 */
inline fun <reified T : ViewModel> Fragment.lazyViewModel(
    noinline providerInitializer: () -> Provider<T>
): LazyViewModelProvider<T> {
    val fragment = this
    return LazyViewModelProvider(
        providerInitializer = providerInitializer,
        viewModelRetriever = { provider: Provider<T> ->
            val factory = SimpleViewModelProviderFactory<T>(provider)
            ViewModelProviders.of(fragment, factory).get(T::class.java)
        }
    )
}

/**
 * Delegating Property that lazily creates a ViewModel or retrieves an existing one (i.e. on screen orientation changes)
 * from [ViewModelProvider].
 */
class LazyViewModelProvider<T : ViewModel>(
    private var viewModelRetriever: ((provider: Provider<T>) -> T)?,
    private var providerInitializer: (() -> Provider<T>)?
) {

    private object UninitializedValue

    private var viewModel: Any? = UninitializedValue

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (viewModel === UninitializedValue) {

            val provider: Provider<T> = providerInitializer!!()
            viewModel = viewModelRetriever!!(provider)

            // Clear initializer references as they are no longer needed once ViewModel is instantiated
            providerInitializer = null
            viewModelRetriever = null
        }
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}

/**
 * Convinient way to access daggers application component
 */
val Fragment.daggerComponent: ApplicationComponent
    get() = (requireActivity().application as TodoApplication).component

class ViewBinderFactoryDelegate<T>(
    private val fragment: Fragment,
    private val factoryAccessor: () -> ViewBinderFactory,
    private val rootViewAccessor: () -> View
) : LifecycleObserver {

    private object UninitializedValue

    private var viewBinder: Any? = UninitializedValue

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun viewDestroyed() {
        viewBinder = UninitializedValue
        fragment.viewLifecycleOwner.lifecycle.removeObserver(this)
    }

    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (viewBinder === UninitializedValue) {
            viewBinder =
                factoryAccessor().get(fragment, rootViewAccessor())
                    ?: throw IllegalArgumentException("No ViewBinder found for $fragment")

            if (fragment.view == null) {
                throw NullPointerException(
                    "View of Fragment is null. " +
                        "You try to access the viewBinder before Fragment.onCreateView() has been called." +
                        "Fragment is $fragment"
                )
            }
            fragment.viewLifecycleOwner.lifecycle.addObserver(this)
        }
        @Suppress("UNCHECKED_CAST")
        return viewBinder as T
    }
}

fun <T> Fragment.getViewBinderFromDagger() = ViewBinderFactoryDelegate<T>(
    fragment = this,
    factoryAccessor = { daggerComponent.viewBinderFactory() },
    rootViewAccessor = { this.view ?: throw IllegalStateException("View has not been set for this fragment yet") }
)