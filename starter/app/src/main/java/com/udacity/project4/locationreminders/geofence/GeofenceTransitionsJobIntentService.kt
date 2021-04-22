package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext


class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    // region Properties
    val remindersLocalRepository: ReminderDataSource by inject()

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    // endregion

    override fun onHandleWork(intent: Intent) {
        // 1. Get event
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // 2. Log error, if any
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // 3. Send notification
        handleEvent(geofencingEvent)
    }

    private fun handleEvent(event: GeofencingEvent) {
        val geofenceTransition = event.geofenceTransition
        val triggeringGeofences = event.triggeringGeofences

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER && triggeringGeofences.isNotEmpty()) {
            sendNotification(triggeringGeofences)
            Log.e(TAG, GEOFENCE_TRANSITION_ENTER)
        } else {
            Log.e(TAG, GEOFENCE_TRANSITION_OTHER)
        }
    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        var firstGeofence = triggeringGeofences.first()
        val requestId = firstGeofence.requestId

        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id)
                )

                // Note: At this time we do not delete the reminder.
            } else {
                // Do nothing
            }
        }
    }

    // region Constants

    companion object {
        private const val TAG = "GeofenceReceiver"
        private const val GEOFENCE_TRANSITION_ENTER = "GeofenceBroadcastReceiver received a geofence transaction of enter"
        private const val GEOFENCE_TRANSITION_OTHER = "GeofenceBroadcastReceiver received a geofence transaction of unexpected type"
        private const val JOB_ID = 4321

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                    context,
                    GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                    intent
            )
        }
    }

    // endregion

}
