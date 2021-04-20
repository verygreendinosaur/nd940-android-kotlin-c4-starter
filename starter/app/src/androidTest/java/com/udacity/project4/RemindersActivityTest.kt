package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test


    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun logoutFromReminders() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // Verify the 'No Data' (no saved reminders) message appears
        onView(withText("No Data")).check(matches(isDisplayed()))

        // Click on the logout button & verify that all the data is correct.
        onView(withId(R.id.logout)).check(matches(withText("Logout")))
        onView(withId(R.id.logout)).perform(click())

        // Click on the logout button & verify that all the data is correct.
        onView(withText("Welcome to the location reminder app")).check(matches(isDisplayed()))
        onView(withId(R.id.login_button)).check(matches(withText("Login")))

        activityScenario.close()
    }

    @Test
    fun addReminder() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        // Verify the 'No Data' (no saved reminders) message appears
        onView(withText("No Data")).check(matches(isDisplayed()))

        // Click on the add reminder button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Add reminder, save
        onView(withId(R.id.reminderTitle)).perform(replaceText("TEST TITLE"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("TEST DESCRIPTION"))
        onView(withId(R.id.saveReminder)).perform(click())


        // Verify the 'No Data' replaced by a reminders list containing the saved reminder
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withText("TEST TITLE")).check(matches(isDisplayed()))
        onView(withText("TEST DESCRIPTION")).check(matches(isDisplayed()))

        activityScenario.close()
    }

}
