package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import getOrAwaitValue
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    // region Test Setup

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var fakeDataSource: FakeDataSource
    lateinit var viewModel: RemindersListViewModel

    @Before
    fun before() {
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
        }

        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    // endregion

    // region Test Teardown

    @After
    fun after() {
        stopKoin()
    }

    // endregion

    @Test
    fun loadReminders_loadsNoRemindersInRemindersList_whenNoReminders() {
        // Given

        // When loadReminders called and no reminders in data source
        viewModel.loadReminders()

        // Then remindersList is also empty
        assertEquals(viewModel.remindersList.getOrAwaitValue().isEmpty(), true)
    }

    @Test
    fun loadReminders_loadsRemindersInRemindersList_whenRemindersExist() = mainCoroutineRule.runBlockingTest {
        // Given

        val firstReminder = ReminderDTO("TEST TITLE A", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)
        val secondReminder = ReminderDTO("TEST TITLE B", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)


        fakeDataSource.saveReminder(firstReminder)
        fakeDataSource.saveReminder(secondReminder)

        // When loadReminders called and no reminders in data source
        viewModel.loadReminders()

        // Then remindersList includes both reminders
        assertEquals(viewModel.remindersList.getOrAwaitValue().size, 2)
    }

}