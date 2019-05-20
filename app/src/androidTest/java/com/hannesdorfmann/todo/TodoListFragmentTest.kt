package com.hannesdorfmann.todo

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.hannesdorfmann.todo.create.State
import com.hannesdorfmann.todo.domain.TodoItem
import com.hannesdorfmann.todo.list.TodoListAdapter
import com.hannesdorfmann.todo.list.TodoListStateMachine
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TodoListFragmentTest {

    @Rule
    @JvmField
    var activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun newTodoItemIsShownInTodoList() {
        val itemToAdd = TodoItem("1", "A", false)
        given {
            prefilledTodoItmes = emptyList()

            todoList {
                assertLoadingState
                assertEmptyListState

                clickCreateTodoItem()

                createItem {

                    assertEnterTitleState("")
                    assertEnterTitleState("")

                    enterTitle("A")
                    assertEnterTitleState("A")

                    pressNext()
                    assertSummaryState("A")

                    pressSave()
                    assertSavingInProgressState("A")
                    assertSavingSuccessfulState("A")
                }

                assertEmptyListState
                assertContentState + itemToAdd

            }
        }
    }

    @Test
    fun markAsDone() {
        val prefilled = listOf(TodoItem("2", "Some Item", false), TodoItem("3", "done item", true))
        given {
            prefilledTodoItmes = prefilled

            todoList {
                assertLoadingState
                assertContentState + prefilled

                clickFirstItem()

                assertContentStateWithFirstItemDone

                clickFirstItem()

                assertContentStateWithFirstItemNotDone
            }
        }
    }

    private fun given(configBlock: ScreenConfig.() -> Unit) {
        val config = ScreenConfig(activityRule)
        configBlock(config)
    }
}

private class ScreenConfig(val activityTestRule: ActivityTestRule<MainActivity>) {
    var prefilledTodoItmes = emptyList<TodoItem>()

    fun todoList(block: TodoListRobot.() -> Unit) {
        val robot = TodoListRobot()
        TestApplication.todoRepository.clear()
        prefilledTodoItmes.forEach {
            TestApplication.todoRepository.add(it)
        }
        activityTestRule.launchActivity(null)
        block(robot)
    }
}

private class TodoListRobot {

    inner class ContentStateBuilder(initialList: List<TodoItem> = emptyList()) {
        var previousContentState = TodoListStateMachine.State.Content(initialList)
        operator fun plus(todoItem: TodoItem) {
            val newContentState = TodoListStateMachine.State.Content(previousContentState.items + todoItem)
            stateVerifier.assertNextState(newContentState)
            previousContentState = newContentState
        }

        operator fun plus(todoItem: List<TodoItem>) {
            val newContentState = TodoListStateMachine.State.Content(previousContentState.items + todoItem)
            stateVerifier.assertNextState(newContentState)
            previousContentState = newContentState
        }
    }

    init {
        ListenableTodoListViewBinder.stateChangeListener = this::stateChanged
    }

    private val stateHistory = ReplayRelay.create<TodoListStateMachine.State>()
    private val stateVerifier = StateVerifier(stateHistory)

    private fun stateChanged(state: TodoListStateMachine.State) {
        stateHistory.accept(state)
    }

    val assertContentState = ContentStateBuilder()

    val assertLoadingState: Unit
        get() = stateVerifier.assertNextState(TodoListStateMachine.State.Loading)

    val assertEmptyListState: Unit
        get() = stateVerifier.assertNextState(TodoListStateMachine.State.Content(emptyList()))

    val assertContentStateWithFirstItemDone: Unit
        get() {
            val firstItem = assertContentState.previousContentState.items[0]
            val firstItemChecked = firstItem.copy(done = true)
            val expectedList = ArrayList<TodoItem>()
            expectedList.add(firstItemChecked)
            expectedList.addAll(
                assertContentState.previousContentState.items.subList(
                    1,
                    assertContentState.previousContentState.items.size
                )
            )
            stateVerifier.assertNextState(assertContentState.previousContentState.copy(expectedList))
        }

    val assertContentStateWithFirstItemNotDone: Unit
        get() {
            val firstItem = assertContentState.previousContentState.items[0]
            val firstItemChecked = firstItem.copy(done = false)
            val expectedList = ArrayList<TodoItem>()
            expectedList.add(firstItemChecked)
            expectedList.addAll(
                assertContentState.previousContentState.items.subList(
                    1,
                    assertContentState.previousContentState.items.size
                )
            )
            stateVerifier.assertNextState(assertContentState.previousContentState.copy(expectedList))
        }

    fun clickCreateTodoItem() {
        Espresso.onView(ViewMatchers.withId(R.id.newItem))
            .perform(ViewActions.click())
    }

    fun createItem(block: CreateItemRobot.() -> Unit) {
        val robot = CreateItemRobot()
        block(robot)
    }

    fun clickFirstItem() {

        Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<TodoListAdapter.TodoViewHolder>(
                    0,
                    clickChildViewWithId(R.id.checkbox)
                )
            )
    }
}

private class CreateItemRobot {
    private val stateVerifier = StateVerifier(ListenableCreateTodoItemViewBinder.INSTANCE.states)

    fun assertEnterTitleState(title: String) {
        stateVerifier.assertNextState(State.EnterTextState(title))
    }

    fun assertSummaryState(title: String) {
        stateVerifier.assertNextState(State.SummaryState(title, false))
    }

    fun assertSavingInProgressState(title: String) {
        stateVerifier.assertNextState(State.SavingInProgressSate(title))
    }

    fun assertSavingSuccessfulState(title: String) {
        stateVerifier.assertNextState(State.SuccessfulSavedState(title))
    }

    fun enterTitle(title: String) {
        Espresso.onView(ViewMatchers.withId(R.id.step1Title))
            .perform(ViewActions.typeText(title))
    }

    fun pressNext() {
        Espresso.onView(ViewMatchers.withId(R.id.button))
            .perform(ViewActions.click())
    }

    fun pressSave() {
        Espresso.onView(ViewMatchers.withId(R.id.create))
            .perform(ViewActions.click())
    }
}

private class StateVerifier<S>(private val stateObservable: Observable<S>) {
    private var alreadyVerifiedStates: List<S> = emptyList()

    @Synchronized
    fun assertNextState(
        nextExpectedState: S
    ) {

        val expectedRendered = alreadyVerifiedStates + nextExpectedState
        val actuallyRendered = stateObservable
            .take(alreadyVerifiedStates.size + 1L)
            .timeout(10, TimeUnit.SECONDS)
            .toList()
            .blockingGet()

        Assert.assertEquals(expectedRendered, actuallyRendered)
        alreadyVerifiedStates = expectedRendered
    }
}

private fun clickChildViewWithId(@IdRes id: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return null
        }

        override fun getDescription(): String {
            return "Click on a child view with specified id = $id."
        }

        override fun perform(uiController: UiController, view: View) {
            val v = view.findViewById<View>(id)
            v.performClick()
        }
    }
}
