package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    // region Test Setup

    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()

        repository =
                RemindersLocalRepository(
                        database.reminderDao(),
                        Dispatchers.Main
                )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // endregion

    // region saveReminder, getReminderById

    @Test
    fun getReminderById_returnsError_whenNoRemindersInDataSource() = runBlocking {
        // GIVEN no reminders saved

        // WHEN getReminders called
        val result = repository.getReminder("DOES NOT EXIST")

        // THEN returns Error
        when (result) {
            is Result.Success<*> -> fail()
            is Result.Error -> assert(true)
        }
    }

    @Test
    fun saveReminderThenGetReminderById_returnsCorrectReminder_whenReminderIdIsSavedReminder() = runBlocking {

        // GIVEN a reminder saved
        repository.saveReminder(reminderA)

        // WHEN getReminders called with id of saved reminder
        val result = repository.getReminder(reminderA.id)

        // THEN returns Success with correct reminder
        when (result) {
            is Result.Success<*> -> assertEquals(result.data as ReminderDTO, reminderA)
            is Result.Error -> fail()
        }
    }

    // endregion

    // region saveReminder, getReminders
    @Test
    fun getReminders_returnsNoReminders_whenNoRemindersInDataSource() = runBlocking {

        // GIVEN no reminders saved

        // WHEN getReminders called
        val result = repository.getReminders()

        // THEN returns Success with empty list
        when (result) {
            is Result.Success<*> -> {
                val results = result.data as List<ReminderDTO>
                assert(results.isEmpty())
            }
            is Result.Error -> fail()
        }
    }

    @Test
    fun saveRemindersThenGetReminders_returnsReminders_whenRemindersInDataSource() = runBlocking {

        // GIVEN two reminders saved

        repository.saveReminder(reminderA)
        repository.saveReminder(reminderB)

        // WHEN getReminders called
        val result = repository.getReminders()

        // THEN returns Success with correct results
        when (result) {
            is Result.Success<*> -> {
                val results = result.data as List<ReminderDTO>
                assertEquals(results.size, 2)
            }
            is Result.Error -> fail()
        }
    }

    // endregion

    // region deleteReminderById

    @Test
    fun deleteReminderById_doesNotCrash_whenIdNotInDataSource() = runBlocking {

        // GIVEN reminder with id not saved

        // WHEN deleteReminder called with id of saved reminder
        repository.deleteReminder(reminderA.id)

        // THEN does not crash
        assert(true)
    }

    @Test
    fun deleteReminderById_deletesCorrectReminder_fromDataSource() = runBlocking {

        // GIVEN a reminder saved
        repository.saveReminder(reminderA)

        // WHEN deleteReminder called with id of saved reminder
        repository.deleteReminder(reminderA.id)

        // THEN correct reminder removed from data source
        val result = repository.getReminder(reminderA.id)

        when (result) {
            is Result.Success<*> -> fail()
            is Result.Error -> assert(true)
        }
    }

    // endregion

    // region deleteAllReminders

    @Test
    fun deleteAllReminders_doesNotCrash_whenNoRemidnersInDataSource() = runBlocking {

        // GIVEN no reminders saved

        // WHEN deleteReminder called with id of saved reminder
        repository.deleteAllReminders()

        // THEN does not crash
        assert(true)
    }

    @Test
    fun deleteAllReminders_deletesAllReminders_fromDataSource() = runBlocking {

        // GIVEN reminders saved
        repository.saveReminder(reminderA)
        repository.saveReminder(reminderB)

        // WHEN deleteReminder called with id of saved reminder
        repository.deleteAllReminders()

        // THEN correct reminder removed from data source
        val result = repository.getReminders()

        when (result) {
            is Result.Success<*> -> {
                val results = result.data as List<ReminderDTO>
                assert(results.isEmpty())
            }
            is Result.Error -> fail()
        }
    }

    // endregion

    // region Constants

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

    // endregion

}
