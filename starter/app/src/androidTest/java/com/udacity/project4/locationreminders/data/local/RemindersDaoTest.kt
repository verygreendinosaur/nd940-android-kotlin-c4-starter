package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertTasksAndGetById() = runBlockingTest {

        // GIVEN a reminder is inserted
        database.reminderDao().saveReminder(reminderA)

        // WHEN the single reminder is retrieved by id
        val loadedReminder = database.reminderDao().getReminderById(reminderA.id)

        // THEN the loaded reminder contains expected values
        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertEquals(loadedReminder.id, reminderA.id)
        assertEquals(loadedReminder.title, reminderA.title)
        assertEquals(loadedReminder.description, reminderA.description)
        assertEquals(loadedReminder.location, reminderA.location)
        assertEquals(loadedReminder.latitude, reminderA.latitude)
        assertEquals(loadedReminder.longitude, reminderA.longitude)
    }

    @Test
    fun insertRemindersAndGetAllReminders() = runBlockingTest {

        // GIVEN two reminders are inserted
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
        database.reminderDao().saveReminder(reminderA)
        database.reminderDao().saveReminder(reminderB)

        // WHEN the single reminder is retrieved by id
        val loadedReminders = database.reminderDao().getReminders()

        // THEN the loaded reminder contains expected values
        assertThat(loadedReminders as List<ReminderDTO>, notNullValue())
        assertEquals(loadedReminders.size, 2)
        assertEquals(loadedReminders.first().id, reminderA.id)
    }

    @Test
    fun insertReminderAndDeleteReminderById() = runBlockingTest {

        // GIVEN two reminders are inserted
        database.reminderDao().saveReminder(reminderA)

        // WHEN the single reminder is deleted by id
        database.reminderDao().deleteReminder(reminderA.id)
        val loadedReminder = database.reminderDao().getReminderById(reminderA.id)

        // THEN the loaded reminder contains expected values
        assertNull(loadedReminder)
    }

    @Test
    fun insertRemindersAndDeleteAllReminders() = runBlockingTest {

        // GIVEN two reminders are inserted
        database.reminderDao().saveReminder(reminderA)
        database.reminderDao().saveReminder(reminderB)

        // WHEN the single reminder is deleted by id
        database.reminderDao().deleteAllReminders()
        val loadedReminders = database.reminderDao().getReminders()

        // THEN the loaded reminder contains expected values
        assert(loadedReminders.isEmpty())
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