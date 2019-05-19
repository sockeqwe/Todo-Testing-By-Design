package com.hannesdorfmann.todo

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.hannesdorfmann.todo.create.State
import com.hannesdorfmann.todo.domain.TodoItem
import com.hannesdorfmann.todo.list.TodoListStateMachine
import io.reactivex.Observable
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
    var activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun newTodoItemIsShownInTodoList() {
        val itemToAdd = TodoItem("1", "A", false)
        given {
            emptyDatabase = true

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

                contentState + itemToAdd

            }
        }
    }
}

private fun given(configBlock: ScreenConfig.() -> Unit) {
    val config = ScreenConfig()
    configBlock(config)
}

private class ScreenConfig {
    var emptyDatabase: Boolean = true

    fun todoList(block: TodoListRobot.() -> Unit) {
        val robot = TodoListRobot()
        block(robot)
    }
}

private class TodoListRobot {

    private val stateVerifier = StateVerifier(RecordingTodoListViewBinder.INSTANCE.states)

    inner class ContentStateBuilder {
        private var previousContentState = TodoListStateMachine.State.Content(emptyList())
        operator fun plus(todoItem: TodoItem) {
            val newContentState = TodoListStateMachine.State.Content(previousContentState.items + todoItem)
            stateVerifier.assertNextState(newContentState)
            previousContentState = newContentState
        }
    }

    val contentState = ContentStateBuilder()

    val assertLoadingState: Unit
        get() = stateVerifier.assertNextState(TodoListStateMachine.State.Loading)

    val assertEmptyListState: Unit
        get() = stateVerifier.assertNextState(TodoListStateMachine.State.Content(emptyList()))

    fun clickCreateTodoItem() {
        Espresso.onView(ViewMatchers.withId(R.id.newItem))
            .perform(ViewActions.click())
    }

    fun createItem(block: CreateItemRobot.() -> Unit) {
        val robot = CreateItemRobot()
        block(robot)
    }
}

private class CreateItemRobot {
    private val stateVerifier = StateVerifier(RecordingCreateTodoItemViewBinder.INSTANCE.states)

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