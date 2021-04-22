package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import com.google.android.gms.location.LocationServices

import java.util.*

class SaveReminderFragment : BaseFragment() {

    // region Properties

    override val primaryViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient


    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // endregion

    // region Lifecycle / Overrides

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = primaryViewModel

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSelectLocationButtonClickListener()
        setSaveReminderButtonClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        primaryViewModel.onClear()
    }

    // endregion

    // Helper methods

    private fun setSelectLocationButtonClickListener() {
        binding.selectLocation.setOnClickListener {
            primaryViewModel.navigationCommand.value = NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
    }

    private fun setSaveReminderButtonClickListener() {
        binding.saveReminder.setOnClickListener {
            val title = primaryViewModel.reminderTitle.value
            val description = primaryViewModel.reminderDescription.value
            val location = primaryViewModel.reminderSelectedLocationStr.value
            val latitude = primaryViewModel.latitude.value
            val longitude = primaryViewModel.longitude.value

            if (latitude != null && longitude != null) {
                val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

                // 1. Create Geofencing request
                addGeofence(latitude, longitude, reminderDataItem.id) {
                    if (it) {
                        primaryViewModel.validateAndSaveReminder(reminderDataItem)
                    } else {
                        primaryViewModel.showErrorMessage.setValue(GEOFENCE_ERROR_TEXT)
                    }
                }
            } else {
                val reminderDataItem = ReminderDataItem(title, description, HOME_TITLE, HOME_LATITUDE, HOME_LONGITUDE)

                // 1. Create Geofencing request
                addGeofence(HOME_LATITUDE, HOME_LONGITUDE, reminderDataItem.id) {
                    if (it) {
                        primaryViewModel.validateAndSaveReminder(reminderDataItem)
                    } else {
                        primaryViewModel.showErrorMessage.setValue(GEOFENCE_ERROR_TEXT)
                    }
                }

            }
        }
    }

    // region Geofencing

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(lat: Double, long: Double, id: String, completion: (didSucceed: Boolean) -> Unit) {

        val geofence = Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(lat, long, GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

        val request = buildGeofencingRequest(geofence)


        geofencingClient?.addGeofences(request, geofencePendingIntent).run {
            addOnSuccessListener {
                completion(true)
            }
            addOnFailureListener {
                completion(false)
            }
        }

    }

    // endregion

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
                "HuntMainActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
        const val GEOFENCE_RADIUS_IN_METERS = 100f
        const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = (12 * 60 * 60 * 1000).toLong()
        const val GEOFENCE_ERROR_TEXT = "Something went wrong - please try again"

        const val HOME_LATITUDE = 47.612665
        const val HOME_LONGITUDE = -122.204228
        const val HOME_TITLE = "Bellevue Downtown Park"
    }

}
