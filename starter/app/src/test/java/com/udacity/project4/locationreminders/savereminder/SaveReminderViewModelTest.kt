package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.util.MainCoroutineRule
import com.udacity.project4.locationreminders.util.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue
import junit.framework.Assert.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SaveReminderViewModelTest {

    // region Test Setup

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var fakeDataSource: FakeDataSource
    lateinit var viewModel: SaveReminderViewModel

    @Before
    fun before() {
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
        }

        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun after() {
        stopKoin()
    }

    // endregion

    // region onClear

    @Test
    fun onClear_clearsSetProperties() {

        // GIVEN a SaveReminderViewModel with some values set
        viewModel.reminderTitle.setValue("TEST TITLE")
        viewModel.reminderDescription.setValue("TEST TITLE")
        viewModel.reminderSelectedLocationStr.setValue("TEST TITLE")
        viewModel.latitude.setValue(40.00)
        viewModel.longitude.setValue(-120.00)


        // WHEN calling onClear
        viewModel.onClear()

        // THEN properties are set to null
        assertEquals(viewModel.reminderTitle.value, null)
        assertEquals(viewModel.reminderDescription.value, null)
        assertEquals(viewModel.reminderSelectedLocationStr.value, null)
        assertEquals(viewModel.latitude.value, null)
        assertEquals(viewModel.longitude.value, null)
    }

    // endregion

    // region validateEnteredData

    @Test
    fun validateEnteredData_returnsFalse_whenTitleNullOrEmpty() {

        // GIVEN a SaveReminderViewModel with no values set

        // WHEN validateEnteredData called with null title
        var reminderDataItem = ReminderDataItem(null, "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)

        // THEN returns false
        assertEquals(viewModel.validateEnteredData(reminderDataItem), false)

        // WHEN validateEnteredData called with empty title
        reminderDataItem = ReminderDataItem("", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)

        // THEN returns false
        assertEquals(viewModel.validateEnteredData(reminderDataItem), false)
    }

    @Test
    fun validateEnteredData_returnsFalse_whenDescriptionNullOrEmpty() {

        // GIVEN a SaveReminderViewModel with no values set

        // WHEN validateEnteredData called with null desc
        var reminderDataItem = ReminderDataItem("TEST TITLE", null, "TEST LOCATION", 10.0, 10.0)

        // THEN returns false
        assertEquals(viewModel.validateEnteredData(reminderDataItem), false)

        // WHEN validateEnteredData called with empty desc
        reminderDataItem = ReminderDataItem("TEST TITLE", "", "TEST LOCATION", 10.0, 10.0)

        // THEN returns false
        assertEquals(viewModel.validateEnteredData(reminderDataItem), false)
    }

    @Test
    fun validateEnteredData_returnsTrue_whenTitleAndDescriptionExist() {

        // GIVEN a SaveReminderViewModel with no values set

        // WHEN validateEnteredData called with both title and description
        var reminderDataItem = ReminderDataItem("TEST TITLE", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)

        // THEN returns true
        assertEquals(viewModel.validateEnteredData(reminderDataItem), true)
    }

    @Test
    fun validateAndSaveReminder_savesData_whenValid() {

        // GIVEN a SaveReminderViewModel with no values set

        // WHEN validateEnteredData called with both title and description
        var reminderDataItem = ReminderDataItem("TEST TITLE", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)
        viewModel.validateAndSaveReminder(reminderDataItem)

        // THEN
        val reminders = fakeDataSource.remindersData
        assertEquals(reminders.isEmpty(), false)
        assertNotNull(reminders[reminderDataItem.id])
    }

    @Test
    fun validateAndSaveReminder_doesNotSaveData_whenTitleMissing() {

        // GIVEN a SaveReminderViewModel with no values set

        // WHEN validateEnteredData called with no titlen
        var reminderDataItem = ReminderDataItem("", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)
        viewModel.validateAndSaveReminder(reminderDataItem)

        // THEN
        val reminders = fakeDataSource.remindersData
        assertEquals(reminders.isEmpty(), true)
        assertNull(reminders[reminderDataItem.id])
    }

    @Test
    fun validateAndSaveReminder_doesNotSaveData_whenDescriptionMissing() {

        // Given a SaveReminderViewModel with no values set

        // When validateEnteredData called with no description
        var reminderDataItem = ReminderDataItem("TEST TITLE", "", "TEST LOCATION", 10.0, 10.0)
        viewModel.validateAndSaveReminder(reminderDataItem)

        // Then
        val reminders = fakeDataSource.remindersData
        assertEquals(reminders.isEmpty(), true)
        assertNull(reminders[reminderDataItem.id])
    }

    // endregion

    // region saveReminder

    @Test
    fun saveReminder_startsAndStopLoadingState_whenSaving() {

        // GIVEN a SaveReminderViewModel with no values set

        // WHEN save called
        mainCoroutineRule.pauseDispatcher()
        var reminderDataItem = ReminderDataItem("TEST TITLE", "TEST DESCRIPTION", "TEST LOCATION", 10.0, 10.0)
        viewModel.saveReminder(reminderDataItem)

        // Then
        assertEquals(viewModel.showLoading.getOrAwaitValue(), true)
        assertNull(viewModel.showToast.value)
        assertNull(viewModel.navigationCommand.value)

        // When saving coroutine finishes
        mainCoroutineRule.resumeDispatcher()

        // Then updates showLoading, showToast, navigationCommand
        assertEquals(viewModel.showLoading.getOrAwaitValue(), false)
        assertEquals(viewModel.showToast.getOrAwaitValue(), "Reminder Saved !")
        assertEquals(viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    // endRegion

}