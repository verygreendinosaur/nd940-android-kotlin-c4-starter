package com.udacity.project4.locationreminders.util

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking

class FakeDataSource : ReminderDataSource {

    // region Properties

    private val observableReminders = MutableLiveData<Result<List<ReminderDTO>>>()
    var remindersData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    // endregion

    // region Override Methods

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(remindersData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        addReminders(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("return the reminder with the id")
    }

    override suspend fun deleteAllReminders() {
        remindersData.clear()
    }

    override suspend fun deleteReminder(id: String) {
        remindersData.remove(id)
    }

    // endregion

    // region Helpers

    suspend fun refreshReminders() {
        observableReminders.value = getReminders()
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            remindersData[reminder.id] = reminder
        }
        runBlocking { refreshReminders() }

    }

    // endregion

}