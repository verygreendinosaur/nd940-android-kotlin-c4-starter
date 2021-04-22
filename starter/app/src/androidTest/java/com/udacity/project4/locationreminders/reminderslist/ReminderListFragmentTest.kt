package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.util.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {

    private lateinit var fakeDataSource: ReminderDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()

        val appModule = module {
            viewModel {
                RemindersListViewModel(
                        ApplicationProvider.getApplicationContext(),
                        get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { FakeDataSource() }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(appModule))
        }

        fakeDataSource = GlobalContext.get().koin.get()

        runBlocking {
            fakeDataSource.deleteAllReminders()
        }
    }

    // endregion

    // region Test Teardown

    @After
    fun after() {
        stopKoin()
    }

    // endregion


    @Test
    fun onLoad_displaysNoReminders_whenNoRemindersSaved() {
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.remindersRecyclerView)).check(matches(hasChildCount(0)))
    }

    @Test
    fun onLoad_displaysReminders_whenRemindersSaved() {
        val firstReminder = ReminderDTO("TEST TITLE A", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)
        val secondReminder = ReminderDTO("TEST TITLE B", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)

        runBlocking {
            fakeDataSource.saveReminder(firstReminder)
            fakeDataSource.saveReminder(secondReminder)
        }

        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.remindersRecyclerView)).check(matches(hasChildCount(2)))
    }

    @Test
    fun clickFab_navigateToSaveReminderFragment() = runBlockingTest {
        // Given on the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // When click the add reminder FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Then navigate to add reminder screen
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    companion object {
        val reminderA = ReminderDTO(
                "TEST TITLE A",
                "TEST DESCRIPTION",
                "TEST LOCATION",
                100.00,
                100.00
        )
        val reminderB = ReminderDTO(
                "TEST TITLE B",
                "TEST DESCRIPTION",
                "TEST LOCATION",
                100.00,
                100.00
        )
    }
}
